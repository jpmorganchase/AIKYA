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
        if (lineData && lineData.values && lineData.versions) {
            return (
                <Grid className={`${classes.root} l_chart`}>
                    <Typography style={{ textAlign: "center" }}>{lineData.name}</Typography>
                    <LineChart
                        sx={{ marginTop: "-55px" }}
                        series={[
                            {
                                data: lineData.values,
                                area: false,
                                highlightScope: {
                                    highlighted: "none",
                                    faded: "none"
                                },
                                color: "#9370DB"
                            }
                        ]}
                        xAxis={[{ data: lineData.versions, scaleType: "point", valueFormatter: toolTipFormat }]}
                        // xAxis={[{ data: lineData.versions, scaleType: "time", valueFormatter: toolTipFormat }]}
                        width={400}
                        height={200}
                    />
                </Grid>
            );
        }

        return <></>;
    };

    return renderLayout();
}
