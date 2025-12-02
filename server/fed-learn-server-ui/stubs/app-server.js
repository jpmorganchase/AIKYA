let express = require("express");
let app = express();
let configs = require("./server.config.js");
var cors = require("cors");
var fs = require("fs");
const path = require("path");

app.options("*", cors());

app.use(function (req, res, next) {
    let origin = req.headers.origin;
    if (configs.ALLOW_ORIGINS.indexOf(origin) > -1) {
        res.setHeader("Access-Control-Allow-Origin", origin);
    }
    res.header("Access-Control-Allow-Methods", "GET, OPTIONS, POST");
    res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    res.header("Access-Control-Allow-Credentials", true);

    next();
});

// Intentional delay for every request
app.use(function (req, res, next) {
    setTimeout(next, 1000);
});

app.use(express.json());

configs.APP_NAME.forEach((node) => {
    app.use(`/${node.NAME}`, express.static(`${node.NAME}`));

    app.get("/", (req, res) => {
        res.end(`${node.NAME} server started @ ${node.PORT}`);
    });

    app.listen(node.PORT, node.SERVER, () => {
        console.log(`server started @ ${node.PORT}`);
    });
});

configs.APP_POST_URLS.forEach((item) => {
    app.post(item.url, (req, res) => {
        readData(res, item.path);
    });
});

app.use(`/orchestrator-server/api/dashboard`, express.static(`pmtfraud/dashboard.json`));
app.use(`/orchestrator-server/api/getuser`, express.static(`pmtfraud/user.json`));

app.use(`/orchestrator-server/api/getLatestGlobalModel`, express.static(`pmtfraud/latestGlobalModel.json`));
app.use(`/orchestrator-server/api/getPendingWorkflow`, express.static(`pmtfraud/getPendingWorkflow.json`));
app.use(`/orchestrator-server/api/getInitialDataSeeds`, express.static(`pmtfraud/getInitialDataSeeds.json`));
app.use(`/orchestrator-server/api/getSummaryChart`, express.static(`pmtfraud/getSummaryChart.json`));
app.use(`/orchestrator-server/api/:id/performances`, express.static(`pmtfraud/getPerformances.json`));
app.use(`/orchestrator-server/api/:id/contributions`, express.static(`pmtfraud/getContributions.json`));
app.use(`/orchestrator-server/api/domains`, express.static(`pmtfraud/domains.json`));
app.use(`/orchestrator-server/api/strategies`, express.static(`pmtfraud/strategies.json`));
app.use(`/orchestrator-server/api/reset`, express.static(`pmtfraud/user.json`));

let counter = 1;
let node = 1;
let isTimerStart = false;
app.get(`/orchestrator-server/api/:id/latestWorkflowSummary`, (request, response) => {
    // console.log(request);
    const filePath = path.resolve("pmtfraud/latestWorkflowSummary.json");
    let data = "";
    if (fs.existsSync(filePath)) {
        data = JSON.parse(fs.readFileSync(filePath), "utf-8");
    } else {
        // console.log(filePath);
    }
    if (counter >= 8) {
        if (!isTimerStart) {
            isTimerStart = true;
            setTimeout(() => {
                node++;
                counter = 1;
                if (node >= 4) {
                    node = 1;
                }
                isTimerStart = false;
            }, 15000);
        }
    } else {
        counter++;
    }
    data.tagSeq = `s${node}-${counter}`;
    return response.send(data);
});

function readData(res, file) {
    let html = fs.readFileSync(file);
    res.writeHead(200, { "Content-Type": "applciation/json" });
    res.end(html);
}
