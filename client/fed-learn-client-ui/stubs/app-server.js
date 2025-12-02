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

app.use(`/orchestrator-cli/api/:type/dashboard`, express.static(`pmtfraud/dashboard.json`));

app.use(`/pmtfraud/user`, express.static(`pmtfraud/user.json`));
app.use(`/orchestrator-cli/api/:type/batch-grid/9e563669`, express.static(`pmtfraud/payment/batch/9e563669.json`));
app.use(`/orchestrator-cli/api/:type/batch-grid/689fb349`, express.static(`pmtfraud/payment/batch/689fb349.json`));
app.use(`/orchestrator-cli/api/:type/batch-grid/:id`, express.static(`pmtfraud/payment/batch/e2b21c49.json`));

app.use(`/orchestrator-cli/api/:type/8501486208`, express.static(`pmtfraud/payment/8145650888.json`));
app.use(`/orchestrator-cli/api/:type/9621690553`, express.static(`pmtfraud/payment/481.json`));

// app.use(`/orchestrator-cli/api/:type/batch/:id`, express.static(`pmtfraud/payment/batch/e2b21c49.json`));
app.use(`/orchestrator-cli/api/:type/getLatestGlobalModel`, express.static(`pmtfraud/latestGlobalModel.json`));
app.use(`/orchestrator-cli/api/:type/getPendingWorkflow`, express.static(`pmtfraud/getPendingWorkflow.json`));
app.use(`/orchestrator-cli/api/:type/getInitialDataSeeds`, express.static(`pmtfraud/getInitialDataSeeds.json`));
app.use(`/orchestrator-cli/api/:type/getSummaryChart`, express.static(`pmtfraud/getSummaryChart.json`));
app.use(`/orchestrator-cli/api/domains`, express.static(`pmtfraud/domains.json`));

app.use(`/orchestrator-cli/api/:type/worklflowRunModel`, express.static(`pmtfraud/getWorkingModel.json`));
app.use(`/orchestrator-cli/api/:type/globalWeightRunModel`, express.static(`pmtfraud/getGlobalWeightRunModel.json`));

app.use(`/orchestrator-cli/api/:type/:id`, express.static(`pmtfraud/payment/81.json`));
app.use(`/orchestrator-cli/api/reset`, express.static(`pmtfraud/payment/81.json`));
app.use(`/orchestrator-cli/api/:type/shap-values/c1e10966`, express.static(`pmtfraud/payment/batch/shaply.json`));

let counter = 1;
let node = 1;
let isTimerStart = false;
app.get(`/orchestrator-cli/apilatestWorkflowSummary`, (request, response) => {
    // console.log(request);
    const filePath = path.resolve("pmtfraud/latestWorkflowSummary.json");
    let data = "";
    if (fs.existsSync(filePath)) {
        data = JSON.parse(fs.readFileSync(filePath), "utf-8");
    } else {
        // console.log(filePath);
    }
    if (counter >= 6) {
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
    data.tag = `s${node}-${counter}.png`;
    return response.send(data);
});

function readData(res, file) {
    let html = fs.readFileSync(file);
    res.writeHead(200, { "Content-Type": "applciation/json" });
    res.end(html);
}
