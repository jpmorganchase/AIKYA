const getHost = () => {
    if (process.env.REACT_APP_ENV === "prod") {
        return "https://" + window.location.hostname + "/orchestrator-server/api";
    } else if (process.env.REACT_APP_ENV === "docker") {
        return "http://" + window.location.hostname + ":9000/orchestrator-server/api";
    } else if (process.env.REACT_APP_ENV === "docker-demo") {
        return "https://server-demo.aikya.network/orchestrator-server/api";
    } else if (process.env.REACT_APP_ENV === "docker-dist") {
        return "https://server-dist.aikya.network/orchestrator-server/api";
    } else {
        return "http://localhost:8080/orchestrator-server/api";
    }
};

const getAPIHost = () => {
    if (process.env.REACT_APP_ENV === "prod") {
        // return "https://" + window.location.hostname + "/pmtfraud-api/api";
        return "https://" + window.location.hostname + "/orchestrator-server/api";
    } else if (process.env.REACT_APP_ENV === "docker") {
        return "http://" + window.location.hostname + ":9000/orchestrator-server/api";
    } else if (process.env.REACT_APP_ENV === "docker-demo") {
        return "https://server-demo.aikya.network/orchestrator-server/api";
    }  else if (process.env.REACT_APP_ENV === "docker-dist") {
        return "https://server-dist.aikya.network/orchestrator-server/api";
    } else {
        return "http://localhost:8080/orchestrator-server/api";
    }
};

const getAggHost = () => {
    if (process.env.REACT_APP_ENV === "prod") {
        return "https://" + window.location.hostname + "/orchestrator-server/api";
    } else if (process.env.REACT_APP_ENV === "docker") {
        return "http://" + window.location.hostname + ":9000/orchestrator-server/api";
    } else if (process.env.REACT_APP_ENV === "docker-demo") {
        return "https://server-demo.aikya.network/orchestrator-server/api";
    }  else if (process.env.REACT_APP_ENV === "docker-dist") {
        return "https://server-dist.aikya.network/orchestrator-server/api";
    } else {
        return "http://localhost:8080/orchestrator-server/api";
      //return "http://localhost:5000";
    }
};

exports.getHost = getHost;
exports.getAPIHost = getAPIHost;
exports.getAggHost = getAggHost;
