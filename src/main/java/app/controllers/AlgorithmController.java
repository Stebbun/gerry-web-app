package app.controllers;

import app.gerry.AlgorithmCore.Algorithm;
import app.gerry.AlgorithmCore.RegionGrowing;
import app.gerry.AlgorithmCore.SimulatedAnnealing;
import app.gerry.Geography.Chunk;
import app.gerry.Geography.District;
import app.gerry.Geography.State;
import app.gerry.Sse.AlgorithmMoveService;
import app.gerry.Sse.SseResultData;
import app.gerry.Util.AlgorithmUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@CrossOrigin
public class AlgorithmController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();
    private State state;
    @Autowired
    AlgorithmMoveService algorithmMoveService;

    @Autowired
    AlgorithmUtil algorithmUtil;
    
    @GetMapping("/algorithm/feed")
    public SseEmitter getFeed() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName();

        if(!userEmitters.containsKey(currentUser)) {
            SseEmitter emitter = new SseEmitter(86400000L);
            userEmitters.put(currentUser, emitter);

            emitter.onCompletion(() -> this.emitters.remove(emitter));
            emitter.onTimeout(() -> {
                emitter.complete();
                this.emitters.remove(emitter);
            });
            return emitter;
        }
        return userEmitters.get(currentUser);
    }

//    @PostMapping("/algorithm/start")
//    @ResponseBody
//    public ResponseEntity initiateAlgorithm(@RequestBody Map<String, Object> params) {
//        Algorithm algorithm = new RegionGrowing(params, algorithmUtil);
//        SseResultData data = new SseResultData("Data", false);
//
//        for(int i = 0; i < 10; i++) {
//            this.emitters.forEach(e -> {
//                try {
//                    Thread.sleep(1000);
//                    e.send(data);
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
//            });
//        }
//        return new ResponseEntity(HttpStatus.ACCEPTED);
//    }

    @PostMapping("/algorithm/start")
    @ResponseBody
    public ResponseEntity initiateAlgorithm(@RequestBody Map<String, Object> params) {
        //Algorithm algorithm = new SimulatedAnnealing(params, algorithmUtil);
        Algorithm algorithm;
        if(params.get("mode").equals("simulated")){
            algorithm = new SimulatedAnnealing(params, algorithmUtil, state);
        }else{
            algorithm = new RegionGrowing(params, algorithmUtil);
            algorithmMoveService.publishInitialSeedDistrictMoves(algorithm);
        }
        algorithmMoveService.setEndAlgorithm(false);
        algorithmMoveService.runAlgorithm(algorithm);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @PostMapping("/setupState")
    @ResponseBody
    public HashMap initState(@RequestBody Map<String, String> params) {
        //state = algorithmUtil.initializeStateWithRandomSeedDistricts(params.get("state"), 2);
        state = algorithmUtil.initializeStateWithAllDistricts(params.get("state"));
        HashMap hm = new HashMap();
        for(District d: state.getDistricts()){
            for(Chunk c: d.getChunks()){
                hm.put(c.getId(),d.getId());
            }
        }
        return hm;
    }

    @PostMapping("/algorithm/stop")
    @ResponseBody
    public ResponseEntity stopAlgorithm() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName();

        if(userEmitters.containsKey(currentUser)) {
            SseEmitter emitter = userEmitters.get(currentUser);
            emitter.complete();
            userEmitters.remove(currentUser);
        }
        algorithmMoveService.setEndAlgorithm(true);

        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

//    @EventListener
//    public void onAlgorithmMov(SseResultData resultData) {
//        List<SseEmitter> deadEmitters = new ArrayList<>();
//        this.emitters.forEach(emitter -> {
//            try {
//                emitter.send(resultData);
//                if(resultData.isLastOne()) {
//                    System.out.println("Algorithm TERMINATED");
//                    deadEmitters.add(emitter);
//                    emitter.complete();
//                }
//            } catch (Exception e) {
//                deadEmitters.add(emitter);
//            }
//        });
//        this.emitters.remove(deadEmitters);
//    }

    @EventListener
    public void onAlgorithmMove(SseResultData resultData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName();

        if(userEmitters.containsKey(currentUser)) {
            SseEmitter emitter = userEmitters.get(currentUser);
            try {
                emitter.send(resultData);
                if(resultData.isLastOne()) {
                    emitter.complete();
                    userEmitters.remove(currentUser);
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }
}
