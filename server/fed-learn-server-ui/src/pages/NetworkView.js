/* eslint-disable import/no-unresolved */
import React, { useEffect, useState } from "react";
import { Button, Grid, InputLabel, MenuItem, Select, Typography } from "@mui/material";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { importLatestWorkflowSummary } from "src/events/importLatestWorkflowSummary";
import { makeStyles } from "@mui/styles";
import BasicLineChart from "src/components/BasicLineChart";
import { importContributions } from "src/events/importContributions";
import { importGlobalSummaryChart } from "src/events/importGlobalSummaryChart";
import BasicPie from "src/components/BasicPie";
import { Helmet } from "react-helmet";
import {
    gear1Color,
    gear2Color,
    gear3Color,
    gear4Color,
    nodeFunction,
    node1Function,
    node2Function,
    node3Function
} from "src/util/manipulatestyles";
import { importmodelTypeDropdownValues } from "src/events/importmodelTypeDropdownValues";
import { importStrategiesDropdownValues } from "src/events/importStrategiesDropdownValues";
import { Fragment } from "react";
import HourglassEmptyRoundedIcon from "@mui/icons-material/HourglassEmptyRounded";
import { importResetData } from "src/events/importResetData";
import CustomizedDialogs from "src/components/CustomizedDialogs";

const useStyles = makeStyles((theme) => ({
    root: {
        width: "100%",
        height: "100%"
    },
    imgCls: {
        width: "100%",
        height: "100%"
    },
    chart: {
        position: "absolute",
        bottom: "25px",
        left: "25px"
    },
    label: {
        fontSize: ".7rem !important"
    }
}));
const NetworkView = () => {
    const classes = useStyles();

    const [latestWorkflowSummary, setLatestWorkflowSummary] = useState(null);
    const [imageName, setImageName] = useState("");
    const [oldTag, setOldTag] = useState("");

    const [contribution, setContribution] = useState(null);
    const [summaryChartData, setSummaryChartData] = useState(null);

    const [modelTypeValues, setModelTypeValues] = useState(null);
    const [modelTypeSelected, setModelTypeSelected] = useState(null);

    const [workFlowTimer, setWorkFlowTimer] = useState();
    const [contributionTimer, setContributionTimer] = useState();
    const [chartTimer, setChartTimer] = useState();

    const [strategiesValues, setStrategiesValues] = useState(null);
    const [strategySelected, setStrategySelected] = useState("select");

    const [isDialogOpen, setDialogOpen] = useState(false);
    const [dialogContent] = useState({
        id: 3,
        title: "RESET",
        desc: "Are you sure to proceed with Data Reset."
    });

    useEffect(() => {
        fetchModelTypeDropdown();
        fetchStrategiesDropdown();
    }, []);

    useEffect(() => {
        if (workFlowTimer) {
            clearInterval(workFlowTimer);
        }

        if (contributionTimer) {
            clearInterval(contributionTimer);
        }

        if (chartTimer) {
            clearInterval(chartTimer);
        }

        async function fetchLatestWorkflowSummary() {
            try {
                let chartData = await importLatestWorkflowSummary(modelTypeSelected);
                if (chartData && chartData.data) {
                    setLatestWorkflowSummary(chartData.data);
                } else {
                    setLatestWorkflowSummary({});
                }
            } catch (e) {
                console.log("There was an error importing the LatestWorkflowSummary");
            }

            setWorkFlowTimer(
                setTimeout(() => {
                    const userToken = localStorage.getItem(process.env.REACT_APP_BANK + "loggedInfo");
                    if (userToken !== null && userToken !== undefined && userToken !== "") {
                        fetchLatestWorkflowSummary();
                    }
                }, 5000)
            );
        }

        async function fetchContribution() {
            try {
                let chartData = await importContributions(modelTypeSelected);
                if (chartData && chartData.data) {
                    chartData.data.children = chartData.data.contribution.map((i) => {
                        i.label = i.name;
                        return i;
                    });
                    setContribution(chartData.data);
                } else {
                    setContribution({});
                }
            } catch (e) {
                console.log("There was an error importing the fetchContribution");
            }

            setContributionTimer(
                setTimeout(() => {
                    const userToken = localStorage.getItem(process.env.REACT_APP_BANK + "loggedInfo");
                    if (userToken !== null && userToken !== undefined && userToken !== "") {
                        fetchContribution();
                    }
                }, 5000)
            );
        }

        async function fetchSummaryChart() {
            try {
                let chartData = await importGlobalSummaryChart(modelTypeSelected);
                if (chartData && chartData.data) {
                    const data = transformData(chartData.data);

                    setSummaryChartData(data);
                } else {
                    setSummaryChartData(null);
                }
            } catch (e) {
                console.log("There was an error importing the chartData");
            }
            setChartTimer(
                setTimeout(() => {
                    const userToken = localStorage.getItem(process.env.REACT_APP_BANK + "loggedInfo");
                    if (userToken !== null && userToken !== undefined && userToken !== "") {
                        fetchSummaryChart();
                    }
                }, 5000)
            );
        }

        if (modelTypeSelected) {
            fetchSummaryChart();
            fetchLatestWorkflowSummary();
            fetchContribution();
        }
    }, [modelTypeSelected]);

    async function fetchModelTypeDropdown() {
        try {
            let results = await importmodelTypeDropdownValues();
            setModelTypeValues(results.data);
            setModelTypeSelected(results.data[0].name);
        } catch (e) {
            console.log("There was an error getting drop down values");
        }
    }

    async function fetchStrategiesDropdown() {
        try {
            let results = await importStrategiesDropdownValues();
            setStrategiesValues(results.data);
        } catch (e) {
            console.log("There was an error getting strategies drop down values");
        }
    }

    async function fetchResetData() {
        try {
            if (modelTypeSelected) {
                await importResetData(modelTypeSelected);
            }
        } catch (e) {
            console.log("There was an error importing the progress");
        }
    }

    const transformData = (summaryData) => {
        summaryData.performance.values = summaryData.performance.values.map((i) => +i);
        return summaryData;
    };
    useEffect(() => {
        if (latestWorkflowSummary && latestWorkflowSummary.tagSeq !== oldTag) {
            let imageId = latestWorkflowSummary.tagSeq;
            setOldTag(imageId);
            setImageName(imageId);
            manipulateTheme(imageId);
        }
    }, [latestWorkflowSummary, oldTag]);

    useEffect(() => {
        if (imageName && (imageName.indexOf("6") !== -1 || imageName.indexOf("7") !== -1)) {
            let stepNum = imageName.split("-");
            let imageId;
            setTimeout(() => {
                if (imageName.indexOf("6") !== -1) {
                    imageId = `${stepNum[0]}-7`;
                    manipulateTheme(imageId);
                } else if (imageName.indexOf("7") !== -1) {
                    imageId = `${stepNum[0]}-8`;
                    manipulateTheme(imageId);
                    toast.success("Aggregation Completed and sent back to Bank", { autoClose: 2000 });
                }
            }, 2000);
        }
    }, [imageName]);

    const manipulateTheme = (imageNumber) => {
        setImageName(imageNumber);
        if (imageNumber.indexOf("s1-") !== -1) {
            node1Function(imageNumber);
        }
        if (imageNumber.indexOf("s2-") !== -1) {
            node2Function(imageNumber);
        }
        if (imageNumber.indexOf("s3-") !== -1) {
            node3Function(imageNumber);
        }
        if (imageNumber.indexOf("s0") !== -1) {
            nodeFunction(imageNumber);
        }
    };

    const renderStepStage = () => {
        if (imageName) {
            const imgUrl = `/static/images/networkview/${imageName}.png`;
            return <img className={classes.imgCls} src={imgUrl} alt="" />;
        }
        return <></>;
    };

    const getDateTimeFormat = (dateTimeObj) => {
        let loadDate = "";
        if (dateTimeObj) {
            const date = new Date(dateTimeObj);
            loadDate = date.toLocaleString("en-US", { month: "short", day: "numeric", year: "numeric" });
            loadDate = loadDate + "  " + date.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" });
        }
        return loadDate;
    };

    const renderModelInfo = () => {
        let trainingDate = "";
        if (latestWorkflowSummary) {
            trainingDate = getDateTimeFormat(latestWorkflowSummary.latestTrainingDate);
        }
        return (
            <>
                <Typography className={classes.label}>
                    Model Version: {latestWorkflowSummary ? latestWorkflowSummary.versionDisplay : ""}
                </Typography>
                <Typography className={classes.label}>Model Time: {trainingDate}</Typography>
            </>
        );
    };

    const handleChange = (event) => {
        let result = event.target.value;
        setModelTypeSelected(result);
    };

    const handleStrategyChange = (event) => {
        let result = event.target.value;
        setStrategySelected(result);
    };

    const renderDomainTypes = () => {
        if (modelTypeSelected) {
            return (
                <>
                    <InputLabel style={{ marginRight: ".5rem", marginTop: ".3rem" }} id="select-label">
                        Data Type:
                    </InputLabel>
                    <Select
                        onChange={(e) => handleChange(e)}
                        label="Data Type"
                        value={modelTypeSelected}
                        className={"dropDownModels"}
                    >
                        {modelTypeValues &&
                            modelTypeValues.map(function (item, i) {
                                return (
                                    <MenuItem value={item.name} key={i}>
                                        {item.label}
                                    </MenuItem>
                                );
                            })}
                    </Select>
                </>
            );
        }
    };

    const renderStrategies = () => {
        return (
            <>
                <InputLabel style={{ marginRight: ".5rem", marginTop: ".3rem" }} id="select-label">
                    Strategies:
                </InputLabel>
                <Select onChange={(e) => handleStrategyChange(e)} value={strategySelected} className={"dropDownModels"}>
                    <MenuItem value="select">Select</MenuItem>
                    {strategiesValues &&
                        strategiesValues.map(function (item, i) {
                            return (
                                <MenuItem value={item.name} key={i}>
                                    {item.func}
                                </MenuItem>
                            );
                        })}
                </Select>
            </>
        );
    };

    const onResetBtnClick = () => {
        setDialogOpen(true);
    };

    const onCallBackFn = (isCloseIcon) => {
        if (!isCloseIcon) {
            fetchResetData();
        }
        setDialogOpen(false);
    };

    function SubmitButton({ enable, label, onBtnClick, noMargin }) {
        if (enable) {
            return (
                <Fragment>
                    <Button
                        className="qBtn"
                        size="small"
                        variant="contained"
                        color="primary"
                        style={{
                            color: "#fff",
                            backgroundColor: "#355cd3",
                            fontSize: ".8rem",
                            padding: ".2rem .8rem",
                            margin: noMargin ? "auto" : ""
                        }}
                        onClick={() => {
                            onBtnClick();
                        }}
                    >
                        {label}
                    </Button>
                </Fragment>
            );
        } else {
            return (
                <Fragment>
                    <Button
                        className="qBtn"
                        size="small"
                        color="primary"
                        variant="contained"
                        style={{
                            fontSize: ".8rem",
                            padding: ".2rem .8rem"
                        }}
                        disabled
                    >
                        {label}
                        {<HourglassEmptyRoundedIcon className={classes.rotateLoadIcon} />}
                    </Button>
                </Fragment>
            );
        }
    }

    const renderLayout = () => {
        return (
            <Grid item lg={12} md={12} xl={12} xs={12} className={`${classes.root} networkMainContainer`}>
                {/* <ToastContainer /> */}
                <Helmet>
                    <title>Network View</title>
                </Helmet>
                <div className="meta-data-layout">
                    {renderDomainTypes()}
                    {renderStrategies()}
                    <SubmitButton
                        enable={true}
                        label={"Reset Data"}
                        onBtnClick={onResetBtnClick}
                        style={{ float: "right" }}
                    />
                </div>
                {renderStepStage()}
                <div className="container bank1">
                    <svg className={`machine ${gear1Color}`} x="0px" y="0px" viewBox="0 0 645 526">
                        <defs />

                        <g>
                            <path
                                x="-136,996"
                                y="-136,996"
                                className="medium-shadow"
                                d="M402 400v-21l-28-4c-1-10-4-19-7-28l23-17 -11-18L352 323c-6-8-13-14-20-20l11-26 -18-11 -17 23c-9-4-18-6-28-7l-4-28h-21l-4 28c-10 1-19 4-28 7l-17-23 -18 11 11 26c-8 6-14 13-20 20l-26-11 -11 18 23 17c-4 9-6 18-7 28l-28 4v21l28 4c1 10 4 19 7 28l-23 17 11 18 26-11c6 8 13 14 20 20l-11 26 18 11 17-23c9 4 18 6 28 7l4 28h21l4-28c10-1 19-4 28-7l17 23 18-11 -11-26c8-6 14-13 20-20l26 11 11-18 -23-17c4-9 6-18 7-28L402 400zM265 463c-41 0-74-33-74-74 0-41 33-74 74-74 41 0 74 33 74 74C338 430 305 463 265 463z"
                            />
                        </g>
                        <g>
                            <path
                                x="-136,996"
                                y="-136,996"
                                className="medium"
                                d="M392 390v-21l-28-4c-1-10-4-19-7-28l23-17 -11-18L342 313c-6-8-13-14-20-20l11-26 -18-11 -17 23c-9-4-18-6-28-7l-4-28h-21l-4 28c-10 1-19 4-28 7l-17-23 -18 11 11 26c-8 6-14 13-20 20l-26-11 -11 18 23 17c-4 9-6 18-7 28l-28 4v21l28 4c1 10 4 19 7 28l-23 17 11 18 26-11c6 8 13 14 20 20l-11 26 18 11 17-23c9 4 18 6 28 7l4 28h21l4-28c10-1 19-4 28-7l17 23 18-11 -11-26c8-6 14-13 20-20l26 11 11-18 -23-17c4-9 6-18 7-28L392 390zM255 453c-41 0-74-33-74-74 0-41 33-74 74-74 41 0 74 33 74 74C328 420 295 453 255 453z"
                            />
                        </g>
                    </svg>
                </div>
                <div className="container bank2">
                    <svg className={`machine ${gear2Color}`} x="0px" y="0px" viewBox="0 0 645 526">
                        <defs />

                        <g>
                            <path
                                x="-136,996"
                                y="-136,996"
                                className="medium-shadow"
                                d="M402 400v-21l-28-4c-1-10-4-19-7-28l23-17 -11-18L352 323c-6-8-13-14-20-20l11-26 -18-11 -17 23c-9-4-18-6-28-7l-4-28h-21l-4 28c-10 1-19 4-28 7l-17-23 -18 11 11 26c-8 6-14 13-20 20l-26-11 -11 18 23 17c-4 9-6 18-7 28l-28 4v21l28 4c1 10 4 19 7 28l-23 17 11 18 26-11c6 8 13 14 20 20l-11 26 18 11 17-23c9 4 18 6 28 7l4 28h21l4-28c10-1 19-4 28-7l17 23 18-11 -11-26c8-6 14-13 20-20l26 11 11-18 -23-17c4-9 6-18 7-28L402 400zM265 463c-41 0-74-33-74-74 0-41 33-74 74-74 41 0 74 33 74 74C338 430 305 463 265 463z"
                            />
                        </g>
                        <g>
                            <path
                                x="-136,996"
                                y="-136,996"
                                className="medium"
                                d="M392 390v-21l-28-4c-1-10-4-19-7-28l23-17 -11-18L342 313c-6-8-13-14-20-20l11-26 -18-11 -17 23c-9-4-18-6-28-7l-4-28h-21l-4 28c-10 1-19 4-28 7l-17-23 -18 11 11 26c-8 6-14 13-20 20l-26-11 -11 18 23 17c-4 9-6 18-7 28l-28 4v21l28 4c1 10 4 19 7 28l-23 17 11 18 26-11c6 8 13 14 20 20l-11 26 18 11 17-23c9 4 18 6 28 7l4 28h21l4-28c10-1 19-4 28-7l17 23 18-11 -11-26c8-6 14-13 20-20l26 11 11-18 -23-17c4-9 6-18 7-28L392 390zM255 453c-41 0-74-33-74-74 0-41 33-74 74-74 41 0 74 33 74 74C328 420 295 453 255 453z"
                            />
                        </g>
                    </svg>
                </div>
                <div className="container bank3">
                    <svg className={`machine ${gear3Color}`} x="0px" y="0px" viewBox="0 0 645 526">
                        <defs />

                        <g>
                            <path
                                x="-136,996"
                                y="-136,996"
                                className="medium-shadow"
                                d="M402 400v-21l-28-4c-1-10-4-19-7-28l23-17 -11-18L352 323c-6-8-13-14-20-20l11-26 -18-11 -17 23c-9-4-18-6-28-7l-4-28h-21l-4 28c-10 1-19 4-28 7l-17-23 -18 11 11 26c-8 6-14 13-20 20l-26-11 -11 18 23 17c-4 9-6 18-7 28l-28 4v21l28 4c1 10 4 19 7 28l-23 17 11 18 26-11c6 8 13 14 20 20l-11 26 18 11 17-23c9 4 18 6 28 7l4 28h21l4-28c10-1 19-4 28-7l17 23 18-11 -11-26c8-6 14-13 20-20l26 11 11-18 -23-17c4-9 6-18 7-28L402 400zM265 463c-41 0-74-33-74-74 0-41 33-74 74-74 41 0 74 33 74 74C338 430 305 463 265 463z"
                            />
                        </g>
                        <g>
                            <path
                                x="-136,996"
                                y="-136,996"
                                className="medium"
                                d="M392 390v-21l-28-4c-1-10-4-19-7-28l23-17 -11-18L342 313c-6-8-13-14-20-20l11-26 -18-11 -17 23c-9-4-18-6-28-7l-4-28h-21l-4 28c-10 1-19 4-28 7l-17-23 -18 11 11 26c-8 6-14 13-20 20l-26-11 -11 18 23 17c-4 9-6 18-7 28l-28 4v21l28 4c1 10 4 19 7 28l-23 17 11 18 26-11c6 8 13 14 20 20l-11 26 18 11 17-23c9 4 18 6 28 7l4 28h21l4-28c10-1 19-4 28-7l17 23 18-11 -11-26c8-6 14-13 20-20l26 11 11-18 -23-17c4-9 6-18 7-28L392 390zM255 453c-41 0-74-33-74-74 0-41 33-74 74-74 41 0 74 33 74 74C328 420 295 453 255 453z"
                            />
                        </g>
                    </svg>
                </div>
                <div className="container bank4">
                    <svg className={`machine ${gear4Color}`} x="0px" y="0px" viewBox="0 0 645 526">
                        <defs />

                        <g>
                            <path
                                x="-136,996"
                                y="-136,996"
                                className="medium-shadow"
                                d="M402 400v-21l-28-4c-1-10-4-19-7-28l23-17 -11-18L352 323c-6-8-13-14-20-20l11-26 -18-11 -17 23c-9-4-18-6-28-7l-4-28h-21l-4 28c-10 1-19 4-28 7l-17-23 -18 11 11 26c-8 6-14 13-20 20l-26-11 -11 18 23 17c-4 9-6 18-7 28l-28 4v21l28 4c1 10 4 19 7 28l-23 17 11 18 26-11c6 8 13 14 20 20l-11 26 18 11 17-23c9 4 18 6 28 7l4 28h21l4-28c10-1 19-4 28-7l17 23 18-11 -11-26c8-6 14-13 20-20l26 11 11-18 -23-17c4-9 6-18 7-28L402 400zM265 463c-41 0-74-33-74-74 0-41 33-74 74-74 41 0 74 33 74 74C338 430 305 463 265 463z"
                            />
                        </g>
                        <g>
                            <path
                                x="-136,996"
                                y="-136,996"
                                className="medium"
                                d="M392 390v-21l-28-4c-1-10-4-19-7-28l23-17 -11-18L342 313c-6-8-13-14-20-20l11-26 -18-11 -17 23c-9-4-18-6-28-7l-4-28h-21l-4 28c-10 1-19 4-28 7l-17-23 -18 11 11 26c-8 6-14 13-20 20l-26-11 -11 18 23 17c-4 9-6 18-7 28l-28 4v21l28 4c1 10 4 19 7 28l-23 17 11 18 26-11c6 8 13 14 20 20l-11 26 18 11 17-23c9 4 18 6 28 7l4 28h21l4-28c10-1 19-4 28-7l17 23 18-11 -11-26c8-6 14-13 20-20l26 11 11-18 -23-17c4-9 6-18 7-28L392 390zM255 453c-41 0-74-33-74-74 0-41 33-74 74-74 41 0 74 33 74 74C328 420 295 453 255 453z"
                            />
                        </g>
                    </svg>
                </div>
                <div className="graph_sec">
                    <header>Global Model Details *</header>

                    <main>
                        <article>
                            {contribution && contribution !== undefined && <BasicPie pieData={contribution} />}
                        </article>

                        <aside>
                            {summaryChartData && summaryChartData.performance !== undefined && (
                                <BasicLineChart lineData={summaryChartData.performance} />
                            )}
                        </aside>
                    </main>
                    <p style={{paddingTop:'20px', fontSize:'11px'}}> * Future Development</p>
                </div>
                {isDialogOpen && (
                    <CustomizedDialogs isOpen={isDialogOpen} callBackFn={onCallBackFn} dialogContent={dialogContent} />
                )}
            </Grid>
        );
    };

    return renderLayout();
};

export default NetworkView;
