import * as React from "react";
import { PieChart } from "@mui/x-charts/PieChart";
import { Grid, Typography } from "@mui/material";
import { makeStyles } from "@mui/styles";

const useStyles = makeStyles((theme) => ({
    root: {
        margin: "auto",
        marginBottom: "-30px",
        "& text": {
            fontSize: ".7rem !important"
        }
    }
}));

export default function BasicPie({ pieData }) {
    const classes = useStyles();
    const palette = ["#87CEFA", "#DEB887", "#20B2AA"];

    const renderLayout = () => {
        if (pieData) {
            return (
                <Grid className={`${classes.root} chartview`}>
                    <Typography style={{ position: "absolute", marginTop: "15px" }}>{pieData.name}</Typography>
                    <PieChart
                        colors={palette}
                        slotProps={{ legend: { hidden: true } }}
                        series={[
                            {
                                // arcLabel: (item) => `${item.name}`,
                                arcLabelMinAngle: 45,
                                data: pieData.children
                            }
                        ]}
                        width={220}
                        height={190}
                    />
                </Grid>
            );
        }

        return <></>;
    };
    return renderLayout();
}
