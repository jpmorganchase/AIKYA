import React from "react";
import { LineChart } from "@mui/x-charts/LineChart";
import { makeStyles } from "@mui/styles";
import { Grid, Typography } from "@mui/material";

const useStyles = makeStyles((theme) => ({
    root: {
        margin: "auto",
        marginTop: 0
    },
    lineCls: {
        marginTop: "-30px !important"
    }
}));
export default function BasicLineChart({ lineData }) {
    const classes = useStyles();
    const toolTipFormat = (dateTimeObj) => {
        const date = new Date(dateTimeObj);
        const loadDate = date.toLocaleString("en-US", { month: "short", day: "numeric" });
        return loadDate;
    };

    const renderLayout = () => {
        if (lineData && lineData.series && lineData.xaxis) {
            let localData = lineData.series[0].data.map(i=>Number(i));
            let globalData = lineData.series[1].data.map(i=>Number(i));
            let xAxisData = lineData.xaxis;
            return (
                <Grid className={`${classes.root} lineChartwrap`}>
                    <Typography style={{ textAlign: "left", padding:"5px", fontWeight:"bold"}}>Model Performance *</Typography>
                    {/* <div className="chartYaxisLabel">Accuracy
                    <span className="line arrow-left"></span></div> */}
            
                    <LineChart
                        series={[
                            {
                                id: "local",
                                data: localData,
                                label: process.env.REACT_APP_BANK,
                                area: false,
                                highlightScope: {
                                    highlighted: "none",
                                    faded: "none"
                                },
                                color: "#52bdbe"
                            },
                            {
                                id: "global",
                                data: globalData,
                                label: 'Global',
                                area: false,
                                highlightScope: {
                                    highlighted: "none",
                                    faded: "none"
                                },
                                color: "#9370DB",
                                
                            }
                        ]}
                        xAxis={[{ data: xAxisData, scaleType: "point", valueFormatter: toolTipFormat }]}
                        // xAxis={[{ data: lineData.versions, scaleType: "time", valueFormatter: toolTipFormat }]}
                        height={300}
                        style={{margin:'0 auto'}}
                        slotProps={{
                            legend: {
                              direction: 'row',
                              position: { vertical: 'top', horizontal: 'right' },
                              padding: 0,
                            },
                            loadingOverlay: { message: 'Data should be available soon.' },
                            // Custom message for empty chart
                            noDataOverlay: { message: 'Select some data to display.' },
                          }}
                    />
                </Grid>
            );
        }

        return <></>;
    };

    return renderLayout();
}
