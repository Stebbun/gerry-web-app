function colorStyle(color){
        return {
            	fillColor: color,
            	fillOpacity: 0.3,
            	stroke: true,
            	fill: true,
            	color: 'white',
            	weight: 1,
            };
}

var queue = [];
var state = "NOT_INIT";
var move;
var districtMap = {};

var eventSource = null;

var subscribe = function () {
    document.getElementById("tfObjectiveFunction").value = "";
    if(state == "CLOSED"){
        if(state == "NORMAL" || state == "PAUSED"){
            for(var i = 17; i <= 345; i++){
                new_Hampshire.resetFeatureStyle(i);
            }
        }
        state = "NOT_INIT";
    }

    var selectedAlgo = document.getElementById("selected_algo");
    if(selectedAlgo.value === "region") {
        for (var key in precinct_data) {
            var districtId = precinct_data[key];
            var precinctId = key;
            var color = 'white';
            new_Hampshire.setFeatureStyle(precinctId, colorStyle(color))
        }
    }

    eventSource = new EventSource('algorithm/feed');
    var elem = document.getElementById("pauseBtnID");

    eventSource.onmessage = function (e) {
        var move = JSON.parse(e.data);
        var object = {
            id: move.destDistrictId,
            population: move.destDistrictPopulation,
            gain: move.objectiveGain,
            value: move.objectiveValue
        };
        districtMap[move.destDistrictId] = object;
        queue.push(move);
        if(state == "CLOSED"){
           state = "NOT_INIT";
           elem.value = "Pause";
           queue = [];
           eventSource.close();
        }
        if(state == "NORMAL" && queue.length > 0){
            move = queue.shift();
            document.getElementById("tfObjectiveFunction").value = move.objectiveValue;
            var precinctId = move.precinctId;
            var districtId = move.destDistrictId;
            var color = genColor(districtId);
            new_Hampshire.setFeatureStyle(precinctId, colorStyle(color))
        }
    };

    eventSource.onopen = function () {
        if(state == "NOT_INIT"){
            state = "NORMAL";
            startAlgorithm();
        }
        else if(state == "INIT_FROM_STEP"){
            state = "PAUSED";
            elem.value = "Unpause";
            startAlgorithm();
        }
    }

    window.onbeforeunload = function () {
        eventSource.close();
    }
}
var pause = function () {
    var elem = document.getElementById("pauseBtnID");
    if(state == "NOT_INIT"){

    }
    else if(state == "NORMAL"){
        state  = "PAUSED";
        elem.value = "Resume";
    }
    else if(state == "PAUSED"){
        state  = "NORMAL";
        elem.value = "Pause";
    }
}

var make_step = function(){
    if(state == "NOT_INIT"){
        state = "INIT_FROM_STEP"
        subscribe();
    }
    if(state == "PAUSED" && queue.length > 0){
            move = queue.shift();
            document.getElementById("tfObjectiveFunction").value = move.objectiveValue;
            var precinctId = move.precinctId;
            var districtId = move.districtId;
            var color = genColor(districtId);
            new_Hampshire.setFeatureStyle(precinctId, colorStyle(color))
    }
}

var stop = function(){
    state = "CLOSED";
    eventSource.close();
    queue = [];
    eventSource = null;
    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/algorithm/stop', true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.send();
}

var startAlgorithm = function () {
    isAlgoRunning = true;
    console.log("Entered start algo function.")
    var state = document.getElementById("selected_state").value;
    var reock = document.getElementById("reock").value;
    var polsbyPopper = document.getElementById("polsbyPopper").value;
    var convexHull = document.getElementById("convexHull").value;
    var politicalFairness = document.getElementById("politicalFairness").value;
    var populationEquality = document.getElementById("populationEquality").value;
	var seedCount = document.getElementById("seedCount").value;
	var algo = document.getElementById("selected_algo").value;
	//algo can be "region" or "simulated"
    var params = {
        state: state,
        reock: reock,
        polsbyPopper: polsbyPopper,
        convexHull: convexHull,
        politicalFairness: politicalFairness,
        populationEquality: populationEquality,
		seedCount: seedCount,
		mode: algo
    };

    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/algorithm/start', true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function() {
        if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
            // Request finished. Do processing here.
            console.log("Algorithm ")
        }
    }
    xhr.send(JSON.stringify(params));
}