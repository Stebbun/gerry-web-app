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

var subscribe = function () {
    var eventSource = new EventSource('algorithm/feed');

    eventSource.onmessage = function (e) {
        var move = JSON.parse(e.data);
        var precinctId = move.precinctId;
        var districtId = move.districtId;
        var color = genColor(districtId);
        new_Hampshire.setFeatureStyle(precinctId, colorStyle(color))
        console.log(precinctId);
        console.log(districtId);
    };

    eventSource.onopen = function () {
        startAlgorithm();
    }

    window.onbeforeunload = function () {
        eventSource.close();
    }
}

var startAlgorithm = function () {
    console.log("Entered start algo function.")
    var state = document.getElementById("selected_state").value;
    var reock = document.getElementById("reock").value;
    var polsbyPopper = document.getElementById("polsbyPopper").value;
    var convexHull = document.getElementById("convexHull").value;
    var politicalFairness = document.getElementById("politicalFairness").value;
    var populationEquality = document.getElementById("populationEquality").value;
    var params = {
        state: state,
        reock: reock,
        polsbyPopper: polsbyPopper,
        convexHull: convexHull,
        politicalFairness: politicalFairness,
        populationEquality: populationEquality
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