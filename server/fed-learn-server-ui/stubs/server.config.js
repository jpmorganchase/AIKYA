const API_SERVER_PATH = "http://localhost:8100/orchestrator-server/api/v1";
const allowedOrigins = ["http://127.0.0.1:4200", "http://localhost:3000"];

const appNamePorts = [
    {
        NAME: "pmtfraud",
        PORT: "8080",
        SERVER: "127.0.0.1"
    }
];

const postURLs = [
    { url: "/orchestrator-server/api/txnTaskProgressStat", path: "pmtfraud/txnTaskProgressStat.json" },
    { url: "/orchestrator-server/api/totalTxnsGrowth", path: "pmtfraud/totalTxnsGrowth.json" },
    { url: "/orchestrator-server/api/signin/agg", path: "pmtfraud/signin.json" },
    { url: "/orchestrator-server/api/signin/bank1", path: "pmtfraud/signin.json" },
    { url: "/orchestrator-server/api/signup/bank1", path: "pmtfraud/signup.json" },
    { url: "/orchestrator-server/api/signin/bank2", path: "pmtfraud/signin.json" },
    { url: "/orchestrator-server/api/signup/bank2", path: "pmtfraud/signup.json" },
    { url: "/orchestrator-server/api/signin/bank3", path: "pmtfraud/signin.json" },
    { url: "/orchestrator-server/api/signup/bank3", path: "pmtfraud/signup.json" },
    { url: "/orchestrator-server/api/signin/network", path: "pmtfraud/signin.json" },
    { url: "/orchestrator-server/api/getuser", path: "pmtfraud/user.json" },
    { url: "/orchestrator-server/api/sendFeedback", path: "pmtfraud/sendFeedback.json" },
    { url: "/orchestrator-server/api/loadLocalData", path: "pmtfraud/sendFeedback.json" },
    { url: "/orchestrator-server/api/reset", path: "pmtfraud/sendFeedback.json" }
];

module.exports = {
    ALLOW_ORIGINS: allowedOrigins,
    APP_NAME: appNamePorts,
    APP_POST_URLS: postURLs,
    API_SERVER_PATH
};
