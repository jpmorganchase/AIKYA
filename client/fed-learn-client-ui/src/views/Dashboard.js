import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import style from "styled-components";
import QuickLinks from "../components/QuickLinks";
import ChartWidgets from "../chart/ChartWidgets";
import { importmodelTypeDropdownValues } from "../events/importmodelTypeDropdownValues";
import CarouselSlide from "../components/CarouselSlide";
import { importBatches } from "../events/importBatches";
import TransactionResults from "../views/TransactionResults";
import { importBatchTransactions } from "../events/importBatchTransactions";
import { importShaplyData } from "../events/importShaplyData";

import BatchProcessing from "./BatchProcessing";
import { useOutletContext } from "react-router-dom";
import DatePicker from "../components/DateRangePicker";

import {
  H1,
  H4,
  Accordion,
  AccordionHeader,
  AccordionPanel,
  FlowLayout,
} from "@salt-ds/core";

const MainLayout = style.div`
width: 100%;
height:100%;
`;

const Container = style.div`
width: 100%;
height:100%;
`;

export const Dashboard = (props) => {
  const [modelTypeValues, setModelTypeValues] = useState([]);
  const [modelTypeSelected, setModelTypeSelected] = useState(null);
  const [batches, setBatches] = useState();
  const [batchHeaders, setBatchHeaders] = useState();
  const [transactions, setTransactions] = useState();
  const [transactionsResponse, setTransactionsResponse] = useState();
  const [batchID, setBatchID] = useState([]);
  const [isLoadData, setLoadData] = useState(false);
  const [timer, setTimer] = useState();
  const [reloadWidgets, setReloadWidgets] = useState();
  const [userDetails] = useOutletContext();
  const [selectedFromDate, setSelectedFromDate] = useState(null );
  const [selectedTooDate, setSelectedTooDate] = useState(null );
  const [isDataLoaded, setIsDataLoaded] = useState(false );
  const [shaplyData, setShaplyData]= useState(false );
  
  
  let count = 0;
  let recordCount = 0;

  useEffect(() => {
    async function fetchModelTypeDropdown() {
      try {
        let results = await importmodelTypeDropdownValues();
        setModelTypeValues(results.data);
        setModelTypeSelected(results.data[0].name);
      } catch (e) {
        console.log("There was an error getting drop down values");
      }
    }
    fetchModelTypeDropdown();
  }, []);

  async function fetchBatches(modelTypeSelected, selectedFromDate, selectedTooDate) {
    try {
      let batches = await importBatches(modelTypeSelected, selectedFromDate, selectedTooDate);
      if (count === 0) {
        setBatches(batches.data.data);
        setBatchHeaders(batches.data.headers);
        reloadAllWidgets();
        recordCount = batches.data.data.length;
        count++;
        setTimeout(initialTxn, 1000);
      }
       else if (count > 0 ) {
        setBatches(batches.data.data);
        reloadAllWidgets();
        setBatchHeaders(batches.data.headers);
        recordCount = batches.data.data.length;
        count++;
        setTimeout(initialTxn, 1000);
      }

      //    fetchLatestGlobalModel();
    } catch (e) {
      console.log("There was an error importing the batches");
    }
  }

  async function initialTxn() {
    let batchesInitialLoad = await importBatches(modelTypeSelected);
    let batchIDs = batchesInitialLoad;

    let modalType = modelTypeSelected === " " ? "payment" : modelTypeSelected;
    if (batchIDs.data && batchIDs.data.data.length > 0) {
        let selectedId = batchID.length === 0 ? batchIDs.data.data[0].action : batchID;
      let transactions = await importBatchTransactions(
        selectedId,
        modalType
      );
   
      setTransactions(transactions.data);
      setTransactionsResponse(transactions.data);
      let shaplyData = await importShaplyData(selectedId, modelTypeSelected);
      setShaplyData(shaplyData.data); 
     
    }
  }

  async function loadShaply(batchID, modalType){
    let shaplyData = await importShaplyData(batchID, modalType);
    setShaplyData(shaplyData.data); 
  }

  async function fetchTransactions(batchID) {
    let modalType = modelTypeSelected === " " ? "payment" : modelTypeSelected;
    if (batchID !== undefined) {
      if (batchID.length == 0) {
        if (modelTypeSelected !== null) {
          setTimeout(initialTxn, 1000);
          
        }
        
      } else {
        let transactions = await importBatchTransactions(batchID, modalType);
        loadShaply(batchID, modalType);
        setTransactions(transactions.data);
        setTransactionsResponse(transactions.data);
        
      }
    }
  }

 
  async function fetchLoadData(itemSelected) {
    try {
      setLoadData(true);
      let payload = {
        fileName: `${itemSelected}`,
        domainType: modelTypeSelected,
        mockerEnabled: false,
      };
      //   let loadedData =  await importLoadData(payload);
    } catch (e) {
      console.log("There was an error importing the progress");
    }
  }

  useEffect(() => {
    fetchBatches(modelTypeSelected, selectedFromDate, selectedTooDate);
    clearTimers();
    setTimer(
      setInterval(() => {
        fetchBatches(modelTypeSelected, selectedFromDate, selectedTooDate);
      }, 10000)
    );
  }, [modelTypeSelected, selectedTooDate, batchID]);

  useEffect(() => {
    if (batchID !== null) {
      fetchTransactions(batchID);
    //   setInterval(() => {
    //     fetchBatches(modelTypeSelected, selectedFromDate, selectedTooDate);
    //   }, 10000)
    }
  }, [batchID]);

  const clearTimers = () => {
    if (timer) {
      clearInterval(timer);
    }
  };
  const handleChange = (item) => {
    let result = item[0].name;
    setModelTypeSelected(result);
  };
const reloadAllWidgets = () => {
        setReloadWidgets(new Date());
    };
const showAccordian = () =>{
    setIsDataLoaded(!isDataLoaded);
}

  return (
    <MainLayout>
      <Header />
      <Container>
        <H1 style={{paddingBottom: "10px"}}>Operations Analytics View</H1>
        {modelTypeValues &&
          modelTypeValues != undefined &&
          modelTypeValues.length > 0 && (
            <QuickLinks
              modelTypeValues={modelTypeValues}
              modelTypeSelected={setModelTypeSelected}
              isDataLoaded = {setIsDataLoaded}
            />
          )}
      {userDetails &&
      userDetails.user &&
      userDetails.user.roles.indexOf("ROLE_ADMIN") !== -1} {
        <Accordion
                value="globalModelChart"
                id="globalModelChart"
                defaultExpanded="true"
                >
                <AccordionHeader>Analytics View</AccordionHeader>
                <AccordionPanel>
                    <FlowLayout>
                    {modelTypeSelected && modelTypeSelected !== null && shaplyData && shaplyData !== undefined && (
                        <ChartWidgets modelTypeSelected={modelTypeSelected} shaplyData={shaplyData}/>
                    )}
                    <div className={"nextRow"}>
                       <div id='violinChart'>
                        
                       </div>
                       {modelTypeSelected && modelTypeSelected !== null && shaplyData && shaplyData !== undefined && (
                         <>
                        <div class="arrow_left">
                            <div class="line"></div>
                            <div class="arrow_left_mode"></div>
                          </div>  
                          <div class="arrow_right">
                            <div class="line"></div>
                            <div class="arrow_right_mode"></div>
                          </div> 
                          </>
                       )}
                    </div>
                       
                    </FlowLayout>
                </AccordionPanel>
                </Accordion>
      }
        

        <div id="BachesBlock" style={{ margin: "10px 0" }}>
          {/* <AccordionHeader>Prediction Summary</AccordionHeader> */}
          {modelTypeSelected && modelTypeSelected !== null && (
              <Accordion
              value="batchStatus"
              id="batchStatusGroup"
              expanded={isDataLoaded}
              onToggle ={showAccordian}
              >
              <AccordionHeader>Batch Status View</AccordionHeader>
              <AccordionPanel>
                    <BatchProcessing
                    setLoadData={setLoadData}
                    isLoadData={isLoadData}
                    modelTypes={modelTypeValues}
                    modelTypeSelected={modelTypeSelected}
                    handleChange={(e) => handleChange(e)}
                    props
                    />
              </AccordionPanel>
              </Accordion>

            
          )}
        </div>
        <DatePicker selectedFromDate={setSelectedFromDate} selectedTooDate={setSelectedTooDate}/>

               {batches &&
          batches.length === 0 &&
          ( <H4 style={{textAlign : 'center', color:'red'}}>No Batches found </H4>
          )}

        {batches &&
          batches !== null &&
          batches.length > 0 &&
          modelTypeSelected &&
          modelTypeSelected !== null && (
            <div id="TxnBlock">
              <H4 style={{padding : '3px 4px'}}>Transaction Summary</H4>
                
              <CarouselSlide
                batches={batches}
                modelTypeSelected={modelTypeSelected}
                batchIDselected={setBatchID}
              />
                 <TransactionResults
                    transactions={transactions}
                    modelTypeSelected={modelTypeSelected}
                    />
            </div>
          )}
     
      </Container>
    </MainLayout>
  );
};

export default Dashboard;
