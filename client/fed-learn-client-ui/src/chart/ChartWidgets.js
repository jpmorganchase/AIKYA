/* eslint-disable import/no-unresolved */
import { Container, Grid, LinearProgress } from "@mui/material";
import React, { useEffect, useState } from "react";
import BasicLineChart from "../chart/BasicLineChart";
import BasicPie from "../chart/BasicPie";
import { importSummaryChart } from "../events/importSummaryChart";
import { Card} from "@salt-ds/core";
import BarChart from "../chart/BarChart";
import { Violin } from "../chart/Violin_Chart/Violin";
import PlotlyChart from "../chart/Violin_Chart/PlotlyChart";

const ChartWidgets = (props) => {
    const {modelTypeSelected, shaplyData} = props;
    const [contribution, setContribution] = useState(null);
    const [timer, setTimer] = useState();

   

    useEffect(() => {
        fetchContribution();

    }, []);

    async function fetchContribution() {
        try {
            let chartData = await importSummaryChart(modelTypeSelected);
            if (chartData && chartData.data) {
                chartData.data.children = chartData.data.contributions.contribution.map((i) => {
                    i.label = i.name;
                    return i;
                });
                chartData.data.performance.values = chartData.data.performance.series.map((i) => +i);
                setContribution(chartData.data);
            } else {
                setContribution({});
            }
        } catch (e) {
            console.log("There was an error importing the fetchContribution");
        }

        setTimer(
            setTimeout(() => {
                const userToken = localStorage.getItem(process.env.REACT_APP_BANK + "loggedInfo");
                if (userToken !== null && userToken !== undefined && userToken !== "") {
                    fetchContribution();
                }
            }, 50000)
        );
    }
    const renderLayout = () => {
        if (!contribution) {
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
        }
        return (
            <>
            <div className={"topRow"}>
                <Card>
                    {contribution && contribution !== undefined && <BasicLineChart lineData={contribution.performance} /> }
                
                </Card>
                <Card >
                    {/* {contribution && contribution.length !== undefined && <BasicPie pieData={contribution} />} */}
                    <div>
                    <BarChart />
                    </div>
                </Card>
            </div>
            
          
            {shaplyData && shaplyData !== undefined && <PlotlyChart width={1200} height={400} data={shaplyData} /> }
              
            {/* {shaplyData && shaplyData !== undefined && <Violin width={1200} height={400} data={shaplyData} /> } */}
                   
        
            
            
            </>
        );
    };

    return renderLayout();
};

export default ChartWidgets;
