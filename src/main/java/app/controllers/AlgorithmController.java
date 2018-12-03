package app.controllers;

import app.SseTesting.Notification;
import app.SseTesting.NotificationJobService;
import app.gerry.AlgorithmCore.Algorithm;
import app.gerry.AlgorithmCore.RegionGrowing;
import app.gerry.Sse.AlgorithmMoveService;
import app.gerry.Sse.SseResultData;
import app.gerry.Sse.TestAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@CrossOrigin
public class AlgorithmController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Autowired
    AlgorithmMoveService algorithmMoveService;
    
    @GetMapping("/algorithm/feed")
    public SseEmitter getFeed() {
        SseEmitter emitter = new SseEmitter();
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitters.remove(emitter);
        });
        //algorithmMoveService.runAlgorithm(new TestAlgorithm());
        return emitter;
    }

    @PostMapping("/algorithm/start")
    @ResponseBody
    public ResponseEntity initiateAlgorithm(@RequestBody Map<String, Object> params) {
        Algorithm algorithm = new RegionGrowing(params);
        SseResultData data = new SseResultData("Data", false);

        for(int i = 0; i < 10; i++) {
            this.emitters.forEach(e -> {
                try {
                    Thread.sleep(1000);
                    e.send(data);
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            });
        }
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @EventListener
    public void onAlgorithmMove(SseResultData resultData) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        this.emitters.forEach(emitter -> {
            try {
                emitter.send(resultData);
                if(resultData.isLastOne()) {
                    deadEmitters.add(emitter);
                }
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });
        this.emitters.remove(deadEmitters);
    }
}
