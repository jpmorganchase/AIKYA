const API_SERVER_PATH = "http://localhost:8100/orchestrator-cli/api/v1";
const allowedOrigins = ["http://127.0.0.1:4200", "http://localhost:3000"];

const appNamePorts = [
    {
        NAME: "pmtfraud",
        PORT: "8080",
        SERVER: "127.0.0.1"
    }
];

const postURLs = [
    { url: "/orchestrator-cli/api/txnTaskProgressStat", path: "pmtfraud/txnTaskProgressStat.json" },
    { url: "/orchestrator-cli/api/totalTxnsGrowth", path: "pmtfraud/totalTxnsGrowth.json" },
    { url: "/orchestrator-cli/api/signin/agg", path: "pmtfraud/signin.json" },
    { url: "/orchestrator-cli/api/signin/bank1", path: "pmtfraud/signin.json" },
    { url: "/orchestrator-cli/api/signup/bank1", path: "pmtfraud/signup.json" },
    { url: "/orchestrator-cli/api/signin/bank2", path: "pmtfraud/signin.json" },
    { url: "/orchestrator-cli/api/signup/bank2", path: "pmtfraud/signup.json" },
    { url: "/orchestrator-cli/api/signin/bank3", path: "pmtfraud/signin.json" },
    { url: "/orchestrator-cli/api/signup/bank3", path: "pmtfraud/signup.json" },
    { url: "/orchestrator-cli/api/getuser", path: "pmtfraud/user.json" },
    { url: "/orchestrator-cli/api/sendFeedback", path: "pmtfraud/sendFeedback.json" },
    { url: "/orchestrator-cli/api/loadLocalData", path: "pmtfraud/sendFeedback.json" },
    { url: "/orchestrator-cli/api/reset", path: "pmtfraud/sendFeedback.json" },
    { url: "/orchestrator-cli/api/domains", path: "pmtfraud/domains.json" },
    { url: "/orchestrator-cli/api/toggle/worklflowRunModel/:id", path: "pmtfraud/getWorkingModel.json" },
    { url: "/orchestrator-cli/api/sendGlobalModelAggregate/:id", path: "pmtfraud/sendGlobalModelAggregate.json" },
    { url: "/orchestrator-cli/api/toggle/globalWeightRunModel/:id", path: "pmtfraud/getGlobalWeightRunModel.json" },
  
];

module.exports = {
    ALLOW_ORIGINS: allowedOrigins,
    APP_NAME: appNamePorts,
    APP_POST_URLS: postURLs,
    API_SERVER_PATH
};
