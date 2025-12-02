import React from "react";

import {
    Button,
    Dialog,
    DialogActions,
    DialogCloseButton,
    DialogContent,
    DialogHeader,
    Dropdown,
    Option,
  } from "@salt-ds/core";

import { Fragment, useEffect, useState } from "react";
// const config = require("../config");



export default function CustomizedDialogs({ isOpen, callBackFn, dialogContent, dataSeeds }) {
    const [open, setOpen] = useState(false);
    const [isSelected, setSelected] = useState(false);
    const [itemSelected, setItemSelected] = useState("");


    useEffect(() => {
        setOpen(isOpen);
    }, [isOpen]);

    useEffect(() => {
        if (dataSeeds && dataSeeds.length > 0) {
            setItemSelected(dataSeeds[0].fileName);
        }
    }, [dataSeeds]);

    const handleClose = () => {
        callBackFn(true);
    };

    const onProceedEvt = () => {
        if (dialogContent.id === 3) {
            callBackFn(false, itemSelected);
        } else if (itemSelected !== "") {
            callBackFn(false, itemSelected);
        }
    };

    const handleChange = (event, newSelected) => {
        let result = newSelected[0].fileName;
        setItemSelected(result);
        
    };

    const renderCustomDialog = () => {
        return (
            <>
           
        <Dialog open={open} onClose={handleClose} id={'loadData'}  style={{ width: `400px`, border: 0,  padding: '10px' }}>
          <DialogHeader  header ={dialogContent.title}/> 
          <DialogContent>
          
                <h5>{dialogContent.desc}</h5>
          
           
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClose}>Cancel</Button>
            <Button variant="cta" onClick={onProceedEvt}>
            PROCEED
            </Button>
          </DialogActions>
          <DialogCloseButton onClick={handleClose} />
        </Dialog>
      </>
        );
        
    };

    const renderLoadDialog = () => {
        // const loadTypes = [{ label: "Init Load", value: `data_init_${config.getNodeNo()}` }];

        return (
            <>
           
            <Dialog open={open} onClose={handleClose} id={'loadData'}  style={{ width: `400px`, border: 0,  padding: '10px' }}>
              <DialogHeader  header ={dialogContent.title}/> 
              <DialogContent>
              
                    <h5>Select Batch</h5>
                <Dropdown
                           
                           onSelectionChange={handleChange}
                           valueToString = {(item)=> (item.label +' ('+item.anomalyDesc +')')}
                        >
                            {dataSeeds &&
                                dataSeeds.map(function (item) {
                                    return (
                                        <Option key={item.label} value={item} />
                                             
                                    );
                                })}
                               
                        </Dropdown>
               
              </DialogContent>
              <DialogActions>
                <Button onClick={handleClose}>Cancel</Button>
                <Button variant="cta" onClick={onProceedEvt}>
                PROCEED
                </Button>
              </DialogActions>
              <DialogCloseButton onClick={handleClose} />
            </Dialog>
          </>
        );
    };

    if (dialogContent && dialogContent.id === 1) {
        return renderLoadDialog();
    }

    return renderCustomDialog();
}
