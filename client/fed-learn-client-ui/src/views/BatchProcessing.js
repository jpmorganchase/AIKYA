import React, { Fragment, useRef } from "react";
import {
  Box,
  Button,
  Container,
  MenuItem,
  Grid,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Select,
  InputLabel,
} from "@mui/material";
import {
  H4,
} from "@salt-ds/core";
import { useEffect, useState } from "react";
import CircularLoader from "../components/CircularLoader";
import { importPendingWorkflow } from "../events/importPendingWorkflow";
import HourglassEmptyRoundedIcon from "@mui/icons-material/HourglassEmptyRounded";
import { makeStyles } from "@mui/styles";
import { useOutletContext } from "react-router-dom";
import { importWorklflowRunModel } from "../events/importWorklflowRunModel";
import { importToggleWorklflowRunModel } from "../events/importToggleWorklflowRunModel";
import ToggleOnSharpIcon from "@mui/icons-material/ToggleOnSharp";
import ToggleOffSharpIcon from "@mui/icons-material/ToggleOffSharp";
import { importGlobalWeightRunModel } from "../events/importGlobalWeightRunModel";
import { importToggleGlobalWeightRunModel } from "../events/importToggleGlobalWeightRunModel";

const useStyles = makeStyles((theme) => ({
  rotateIcon: {
    animation: "$spin 2s linear infinite",
    color: "blue",
  },
  "@keyframes spin": {
    "0%": {
      transform: "rotate(0deg)",
    },
    "100%": {
      transform: "rotate(360deg)",
    },
  },
  rowCell: {
    width: "150px",
    padding: "3px 0 !important",
    // paddingRight: "10px !important",
    textAlign: "center !important",
    border: "0 !important",
    "&.initial": {
      opacity: ".5 !important",
    },
  },
  rotateLoadIcon: {
    animation: "$spin 2s linear infinite",
    color: "blue",
    marginLeft: "-2rem",
    marginTop: "0rem",
    float: "left",
  },
  dateLabel: {
    fontSize: "1rem",
  },
  toggleType: {
    textTransform: "capitalize",
    fontSize: ".8rem",
    "& span": {
      marginRight: ".3rem",
      fontSize: "1rem",
      color: "rgba(0, 0, 0, 0.6)",
    },
    "& svg": {
      fontSize: "2.5rem",
      cursor: "pointer",
    },
    margin: "0 1rem",
  },
}));

const BatchProcessing = ({
  isLoadData,
  setLoadData,
  modelTypes,
  modelTypeSelected,
  handleChange,
  props,
  ...rest
}) => {
  const [pendingWorkflow, setPendingWorkflow] = useState(null);
  const [batches, setBatches] = useState([]);
  const classes = useStyles();

  const [page] = useState(0);
  const [rowsPerPage] = useState(10);
  const [userDetails] = useOutletContext();
  const [modelTypeValue, setModelTypeValue] = useState("Payment");
  const [workflowModel, setWorkflowModel] = useState();
  const [workModelTypeRadio, setWorkModelTypeRadio] = useState("");

  const [weightModel, setWeightModel] = useState();
  const [weightModelTypeRadio, setWeightModelTypeRadio] = useState("");
  const [timer, setTimer] = useState();


  async function fetchPendingWorkflow() {
    try {
      let batches = await importPendingWorkflow(modelTypeSelected);
      setPendingWorkflow(batches.data);
      setBatches(batches.data.result);
      if (batches.data.result.length > 0) {
        setLoadData(false);
      }
    } catch (e) {
      console.log("There was an error importing the batches");
    }
  }

  useEffect(() => {
    fetchPendingWorkflow();
    clearTimers();
    setTimer(
      setInterval(() => {
        fetchPendingWorkflow();
      }, 5000)
    );
  }, [modelTypeSelected]);

  const clearTimers = () => {
    if (timer) {
      clearInterval(timer);
    }
  };

  useEffect(() => {
    if (modelTypeSelected === "payment") {
      setModelTypeValue("Payment");
    } else if (modelTypeSelected === "credit_card_fraud") {
      setModelTypeValue("Credit");
    } else if (modelTypeSelected === "payment_fraud") {
      setModelTypeValue("Payment Anomalies");
    } else {
      setModelTypeValue(modelTypeSelected);
    }

    async function loadRunmodels() {
      try {
        let data = await importWorklflowRunModel(modelTypeSelected);
        setWorkflowModel(data.data);

        data = await importGlobalWeightRunModel(modelTypeSelected);
        setWeightModel(data.data);
      } catch (e) {
        console.log("There was an error importing the workflowModel");
      }
    }

    loadRunmodels();
  }, [modelTypeSelected]);

  useEffect(() => {
    if (workflowModel) {
      setWorkModelTypeRadio(workflowModel.mode);
    }
  }, [workflowModel]);

  useEffect(() => {
    if (weightModel) {
      setWeightModelTypeRadio(weightModel.mode);
    }
  }, [weightModel]);

  async function toggleWorkflowModelType(name) {
    try {
      if (name === "workflow") {
        let data = await importToggleWorklflowRunModel(workflowModel.id);
        if (data.data && data.data.status !== 500) {
          setWorkflowModel(data.data);
        }
      } else {
        let data = await importToggleGlobalWeightRunModel(weightModel.id);
        if (data.data && data.data.status !== 500) {
          setWeightModel(data.data);
        }
      }
    } catch (e) {
      console.log("There was an error importing the workflowModel");
    }
  }

  const renderBatchHeader = (batch) => {
    if (batch.workflowStatus.toLowerCase() === "pending") {
      return (
        <>
          {/* <span style={{ fontSize: "1.3rem", color: "blue" }}>
                        {batch.batchId} - {batch.workflowStatus} -{" "}
                    </span> */}
          {/* <HourglassEmptyRoundedIcon className={`${classes.rotateIcon}`} id="dumbelInput" /> */}
        </> 
      );
    } else {
      return <></>;
    }
  };

  const getDateTimeFormat = (dateTimeObj) => {
    let loadDate = "";
    if (dateTimeObj) {
      const date = new Date(dateTimeObj);
      loadDate = date.toLocaleString("en-US", {
        month: "short",
        day: "numeric",
      });
      loadDate =
        loadDate +
        "  " +
        date.toLocaleTimeString("en-US", {
          hour: "2-digit",
          minute: "2-digit",
        });
    }
    return loadDate;
  };

  const getDate = () => {
    // let trainingDate = "";
    // if (
    //   pendingWorkflow &&
    //   pendingWorkflow.result &&
    //   pendingWorkflow.result.length > 0
    // ) {
    //   trainingDate = getDateTimeFormat(pendingWorkflow.result[0].trainingDate);
    // }

    let trainingDate =  getDateTimeFormat(new Date());
    return trainingDate;
  };

  const renderStepsLayout = () => {
      if (batches && batches.length > 0) {
    return (
      <>
        <H4 style={{padding : '3px 15px'}}>Current Batch Status: <span className={classes.dateLabel}>{getDate()}</span></H4>
        
        <TableContainer>
          <Table
            stickyHeader
            style={{ overflow: "hidden" }}
            className={"stepsBlock"}
          >
            <TableBody>
              {batches
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                .map((batch, index) => (
                  <TableRow
                    hover
                    key={`${batch.batchId}-${index}`}
                    sx={{ backgroundColor: "#fff !important" }}
                  >
                    {/* <TableCell
                      className={`${classes.rowCell} tdlabel`}
                    ></TableCell> */}
                    {batch.steps.map((step, stepIndex) => (
                      <TableCell
                        key={stepIndex}
                        className={` ${
                          classes.rowCell
                        } ${step.stepStatus.toLowerCase()} tdlabel rowheadColor`}
                      >
                        <span>{step.label}</span>
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              {batches
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                .map((batch, index) => (
                  <TableRow
                    hover
                    key={`${batch.batchId}-${index}`}
                    sx={{ backgroundColor: "rgba(0, 0, 0, 0.04)" }}
                  >
                    {/* <TableCell className={`${classes.rowCell} tdactions`}>
                      {renderBatchHeader(batch)}
                    </TableCell> */}
                    {batch.steps.map((step, stepIndex) => (
                      <TableCell
                        key={stepIndex}
                        className={`${classes.rowCell} tdactions`}
                      >
                        <CircularLoader
                          step={step}
                          totalSteps={batch.steps.length}
                          batch={batch}
                          workflowModel={workflowModel}
                          modelTypeSelected={modelTypeSelected}
                      
                        />
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
            </TableBody>
          </Table>
        </TableContainer>
      </>
    );
     }

    // return <></>;
  };

  

  const handleWorkFlowModelChange = (name, e, setType) => {
    setType(e);
    toggleWorkflowModelType(name);
  };

  const renderWorkFlowModel = (data, type, setType) => {
    if (data.modes && data.modes.length > 1) {
      return (
        <div className={classes.toggleType}>
          <span className="qlabel">{data.name}:</span>
          {data.modes[0]}
          {type === data.modes[0] ? (
            <ToggleOffSharpIcon
            // onClick={() => handleWorkFlowModelChange(data.name, data.modes[1], setType) }
            />
          ) : (
            <ToggleOnSharpIcon
            // onClick={() => handleWorkFlowModelChange(data.name, data.modes[0], setType)}
            />
          )}
          {data.modes[1]}
        </div>
      );
    }

    return <></>;
  };

 

  

  const renderLayout = () => {
    if (pendingWorkflow) {
      return (
        <>
         {batches && batches.length > 0 && (
          <Grid
            item
            lg={12}
            md={12}
            xl={12}
            xs={12}
            sx={{
              backgroundColor: "background.default",
              padding: 0,
              width:'100%',
            }}
          >
            <Grid
              item
              lg={12}
              md={12}
              xl={12}
              xs={12}
              sx={{
                backgroundColor: "background.default",
                padding: 0,
              }}
            >
              <Grid
                item
                lg={12}
                md={12}
                xl={12}
                xs={12}
                style={{ display: "flex" }}
              >
              
              </Grid>
          
             
                <Box
                  sx={{
                    pt: 1,
                    border: "1px solid #dcdcdc",
                    boxShadow: "0 1px 10px #dcdcdc",
                    margin: "0px",
                    pb:2
                  }}
                >
                  {renderStepsLayout()}
                </Box>
             
            </Grid>
          </Grid>
          )}
        </>
      );
    }
    return (
      <Container
        maxWidth={false}
        style={{
          padding: 0
        }}
      >
        <LinearProgress />
      </Container>
    );
  };

  return renderLayout();
};

export default BatchProcessing;
