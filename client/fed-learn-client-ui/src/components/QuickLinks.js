import React, { useState, useEffect } from "react";
import {
    Button,
    Dropdown,
    Option,
    FormFieldLabel
  } from "@salt-ds/core";
  import CustomizedDialogs from "../components/CustomizedDialogs";
  import { importInitialDataSeeds } from "../events/importInitialDataSeeds";
  import { importLoadData } from "../events/importLoadData";

export const QuickLinks = (props) => {
    const {modelTypeValues, modelTypeSelected, isDataLoaded} = props;
    const [isDialogOpen, setDialogOpen] = useState(false);
    const [dialogContent, setDialogContent] = useState({});
    const [dataSeeds, setDataSeeds] = useState({});
    const [isLoadData, setLoadData] = useState(false);
    const [selected, setDefaultSelect] = useState([modelTypeValues[0]]);

    const DIALOG_CONFIGS = {
        LOAD: {
            id: 1,
            title: "Load Transactions",
            desc: "Select Batch",
            componentData: []
        }
    };

    const handleChange = (item) => {
        let result = item[0].name;
        modelTypeSelected(result);
        setDefaultSelect(item);
       
    };
    const onLoadBtnClick = () => {
        setDialogContent(DIALOG_CONFIGS.LOAD);
        // setDialogOpen(true);
        fetchInitialDataSeeds();
    };

    const onCallBackFn = (isCloseIcon, itemSelected) => {
        if (!isCloseIcon) {
            if (dialogContent.id === 1) {
                fetchLoadData(itemSelected);
            } 
        }
        setDialogOpen(false);
        setDialogContent({});
    };
    async function fetchInitialDataSeeds() {
        try {
            if(selected && selected[0].label ) {
                let results = await importInitialDataSeeds(selected[0].name );
                setDataSeeds(results.data);
                setDialogOpen(true);
    
            }
        } catch (e) {
            console.log("There was an error importing the progress");
        }
    }
    async function fetchLoadData(itemSelected) {
        try {
            setLoadData(true);
           
            let payload = {
                fileName: `${itemSelected}`,
                domainType: selected[0].name,
                mockerEnabled:false

            };
            let loadedData =  await importLoadData(payload);
            props.isDataLoaded(true);
            // if(loadedData != null){
            //     setTimeout(reloadAllWidgets, 15000);
              
            // }
           
        } catch (e) {
            console.log("There was an error importing the progress");
        }
    }
    return (
        <div>
            <FormFieldLabel>Domain </FormFieldLabel>
                <Dropdown
                // defaultSelected={modelTypeValues[0].label}
                onSelectionChange={(e, data) => handleChange(data)}
                valueToString={(item) => item && item.label}
                style={{width:'220px'}}
                bordered = {'true'}
                selected = {selected}
        >
          {modelTypeValues &&
            modelTypeValues.map(function (item) {
              return <Option key={item.label} value={item} />;
            })}
        </Dropdown>

        <Button sentiment="accented" appearance="solid"
            //   enable={!isLoadData}
              onClick={onLoadBtnClick}
              className ="q_btns"
            > Load Transactions </Button>

{isDialogOpen && (
                    <CustomizedDialogs
                        isOpen={isDialogOpen}
                        dataSeeds={dataSeeds}
                        callBackFn={onCallBackFn}
                        dialogContent={dialogContent}
                    />
                )}

        </div> 
        
    )
}

export default QuickLinks;