const getHost = () => {
    if (process.env.REACT_APP_ENV === "prod") {
        return "https://" + window.location.hostname + "/orchestrator-cli/api";
    } else if (process.env.REACT_APP_ENV === "docker") {
        const port = getServerNodePort();
        return `http://${window.location.hostname}:${port}/orchestrator-cli/api`;
    } else if (process.env.REACT_APP_ENV === "docker-local") {
        return "http://localhost:8080/orchestrator-cli/api";
    } else if (process.env.REACT_APP_ENV === "docker-demo") {
        return getDemoServerNodeUrl();
    } else if (process.env.REACT_APP_ENV === "docker-dist") {
        return getDistServerNodeUrl();
    } else {
        return "http://localhost:8080/orchestrator-cli/api";
    }
};

const getAPIHost = () => {
    if (process.env.REACT_APP_ENV === "prod") {
        // return "https://" + window.location.hostname + "/pmtfraud-api/api";
        return "https://" + window.location.hostname + "/orchestrator-cli/api";
    } else if (process.env.REACT_APP_ENV === "docker") {
        const port = getServerNodePort();
        return `http://${window.location.hostname}:${port}/orchestrator-cli/api`;
    } else if (process.env.REACT_APP_ENV === "docker-local") {
        return "http://localhost:8080/orchestrator-cli/api";
    } else if (process.env.REACT_APP_ENV === "docker-demo") {
        return getDemoServerNodeUrl();
    } else if (process.env.REACT_APP_ENV === "docker-dist") {
        return getDistServerNodeUrl();
    } else {
        return "http://localhost:8080/orchestrator-cli/api";
    }
};

const getAggHost = () => {
    if (process.env.REACT_APP_ENV === "prod") {
        return "https://" + window.location.hostname + "/orchestrator-cli/api";
    } else if (process.env.REACT_APP_ENV === "docker") {
        const port = getServerNodePort();
        return `http://${window.location.hostname}:${port}/orchestrator-cli/api`;
    } else if (process.env.REACT_APP_ENV === "docker-local") {
        return "http://localhost:8080/orchestrator-cli/api";
    } else if (process.env.REACT_APP_ENV === "docker-demo") {
        return getDemoServerNodeUrl();
    } else if (process.env.REACT_APP_ENV === "docker-dist") {
        return getDistServerNodeUrl();
    } else {
        return "http://localhost:8080/orchestrator-cli/api";
    }
};

const getNodeNo = () => {
    if (process.env.REACT_APP_BANK === "jpm") {
        return "bank1";
    }
    if (process.env.REACT_APP_BANK === "citi") {
        return "bank2";
    }
    if (process.env.REACT_APP_BANK === "dbs") {
        return "bank3";
    }
};
const getServerNodePort = () => {
    /** this is for container env, since all service runs on the same machine, we need assign different port
      add more node and port here in future
     **/
    const BANK_PORTS = {
        "bank1": "8080",
        "bank2": "8081",
        "bank3": "8082"
    };
    //make it case insenstive
    const bankName = process.env.REACT_APP_BANK ? process.env.REACT_APP_BANK.toLowerCase() : "";
    return BANK_PORTS[bankName];
};
const getDemoServerNodeUrl = () => {
    /** this is for demo container env, since all service runs on the same machine, we need assign different port
      add more node and port here in future
     **/
    const BANK_URLS = {
        "bank1": "https://bank1-demo.aikya.network/orchestrator-cli/api",
        "bank2": "https://bank2-demo.aikya.network/orchestrator-cli/api",
        "bank3": "https://bank3-demo.aikya.network/orchestrator-cli/api"
    };
    //make it case insenstive
    const bankName = process.env.REACT_APP_BANK ? process.env.REACT_APP_BANK.toLowerCase() : "";
    return BANK_URLS[bankName];
};
const getDistServerNodeUrl = () => {
    /** this is for dist container env, since all service runs on the same machine, we need assign different port
      add more node and port here in future
     **/
    const BANK_URLS = {
        "bank2": "https://bank2-dist.aikya.network/orchestrator-cli/api",
    };
    //make it case insenstive
    const bankName = process.env.REACT_APP_BANK ? process.env.REACT_APP_BANK.toLowerCase() : "";
    return BANK_URLS[bankName];
};
exports.getHost = getHost;
exports.getAPIHost = getAPIHost;
exports.getNodeNo = getNodeNo;
exports.getAggHost = getAggHost;
