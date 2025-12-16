import React, { useState , useEffect }  from "react";
import { makeStyles } from "@mui/styles";
import { CircularProgress, Fab } from "@mui/material";
import { Check as CheckIcon } from "@mui/icons-material";
import HourglassEmptyRoundedIcon from "@mui/icons-material/HourglassEmptyRounded";
import WatchLaterOutlinedIcon from "@mui/icons-material/WatchLaterOutlined";
import PlayCircleFilledWhiteRoundedIcon from "@mui/icons-material/PlayCircleFilledWhiteRounded";
import { importSendGlobalModelAggregate } from "../../events/importSendGlobalModelAggregate";
import { importPendingWorkflow } from "../../events/importPendingWorkflow";


const useStyles = makeStyles((theme) => ({
    root: {
        display: "flex",
       
        "& button": {
            width: "42px",
            height: "42px"
        },
        "& svg": {
            fontSize: "1.2rem"
        }
    },

    rotateIcon: {
        animation: "$spin 2s linear infinite",
        backgroundColor: "#fff !important",
        color: "blue !important"
    },
    "@keyframes spin": {
        "0%": {
            transform: "rotate(360deg)"
        },
        "100%": {
            transform: "rotate(0deg)"
        }
    },
    wrapper: {
        position: "relative"
    },
    buttonSuccess: {
        backgroundColor: "#6B8E23 !important",
        cursor: "default !important"
    },
    buttonFail: {
        backgroundColor: "red !important",
        cursor: "default !important"
    },
    buttonInitial: {
        backgroundColor: "gray !important",
        opacity: ".5",
        cursor: "default !important"
    },
    fabComplete: {
        color: "#006400 !important",
        position: "absolute !important",
        top: -4,
        left: -4,
        zIndex: 1
    },
    fabProgress: {
        position: "absolute",
        top: -4,
        left: -4,
        zIndex: 1
    },
    fabFail: {
        position: "absolute !important",
        top: -4,
        left: -4,
        zIndex: 1
    },
    arrowCls: {
        margin: "auto !important",
        position: "relative",
        left: "-3px",
        "&::before": {
            content: '"❯"',
            color: "#999"
        }
    },
    pauseIcon: {
        "& svg": {
            fontSize: "3rem",
            color: "dimgray"
        }
    }
}));

export default function CircularLoader({ step, totalSteps, batch, workflowModel, loadSteps  }) {
    const classes = useStyles();
    const [loading, setLoading] = useState(true);
    const timer = React.useRef();
    const [counter, setCounter] = useState(step && step.step === 6 ? 90 : 60);
    const [workFlowmodaldata, setWorkflowModel] = useState({});
   

    timer.current = window.setTimeout(() => {
        if(document.querySelectorAll('.circularSteps .lastHorLine')[3] ){
            document.querySelectorAll('.circularSteps .lastHorLine')[3].style.visibility= 'hidden';
        }
       
        setLoading(false);
        clearTimeout(timer.current);
    }, 1000 * step.step);

    const renderCompleteLoading = () => {
        if (step.stepStatus.toLowerCase() === "complete") {
            if(document.querySelector('#dumbelInput') && document.querySelector('#dumbelInput') !== undefined){
                document.querySelector('#dumbelInput').style.animation = 'none';
            }
           
            return (
                <div className={classes.wrapper}>
                    <Fab color="primary" className={classes.buttonSuccess}>
                        <CheckIcon />
                    </Fab>
                    {loading && <CircularProgress size={50} className={classes.fabComplete} disableShrink />}
                </div>
            );
        }
    };

    async function sendWorkflowModelAggregate() {
        try {
            let data = await importSendGlobalModelAggregate(batch.workflowTraceId);
            setWorkflowModel(data.data);
            setTimeout(loadSteps, 5000);
        } catch (e) {
            console.log("There was an error importing the workflowModel");
        }
    }
    

    const renderPendingLoading = () => {
        
        if ((step.step === 4 || step.step === 6) && step.stepStatus.toLowerCase() === "pending") {
            if(document.querySelector('#dumbelInput') && document.querySelector('#dumbelInput') !== undefined){
                document.querySelector('#dumbelInput').style.animation = 'makeStyles-keyframes-spin-2 2s linear infinite';
            }
            window.setTimeout(() => {
                if (counter < 1) {
                    setCounter(30);
                } else {
                    setCounter(counter - 1);
                }
            }, 1000);
            return (
                <div className={classes.wrapper}>
                    <Fab color="primary">{counter}</Fab>
                    <CircularProgress size={50} className={classes.fabProgress} disableShrink />
                </div>
            );
        }

        if (step.stepStatus.toLowerCase() === "pending") {
            if (step.step === 5 && (workflowModel && workflowModel.mode === "manual")) {
                return renderPauseLoading();
            }

            return (
                <div className={classes.wrapper}>
                    <Fab color="primary" className={classes.rotateIcon}>
                        <HourglassEmptyRoundedIcon />
                    </Fab>
                    <CircularProgress size={50} className={classes.fabProgress} disableShrink />
                </div>
            );
        }
    };

    const renderPauseLoading = () => {
        return (
            <div className={classes.wrapper}>
                <Fab className={`${classes.pauseIcon} palyBtn`} onClick={sendWorkflowModelAggregate}>
                    <PlayCircleFilledWhiteRoundedIcon />
                </Fab>
            </div>
        );
    };

    const renderInitialLoading = () => {
        if (step.stepStatus.toLowerCase() === "initial") {
            return (
                <div className={classes.wrapper}>
                    <Fab color="primary" className={classes.buttonInitial}>
                        {/* <WatchLaterOutlinedIcon /> */}
                    </Fab>
                    {/* <CircularProgress size={50} className={classes.fabInitial} disableShrink /> */}
                </div>
            );
        }
    };

    return (
        <div className={`${classes.root} circularSteps`}>
            {step.step === 1 && <hr style={{ borderTop: "3px dotted transparent", background: "transparent" }} />}
            {step.step !== 1 && <hr />}
            {/* {step.step !== 1 && <span className={classes.arrowCls}></span>} */}
            {renderPendingLoading()}
            {renderCompleteLoading()}
            {renderInitialLoading()}
            {step.step !== totalSteps && <hr className='lastHorLine' />}
            {step.step === totalSteps && (
                <hr style={{ borderTop: "3px dotted transparent", background: "transparent" }} />
            )}
        </div>
    );
}
