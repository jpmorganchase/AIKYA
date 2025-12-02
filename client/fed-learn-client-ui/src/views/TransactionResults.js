import React, { useState, useEffect , useMemo, useRef, useLayoutEffect} from "react";
import { AgGridReact } from "ag-grid-react";
import { importSingleTransaction } from "../events/importSingleTransaction";
import "ag-grid-enterprise";
import "@ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-grid.css";
import "@salt-ds/ag-grid-theme/salt-ag-theme.css";
import { useAgGridHelpers } from "../util/useAgGridHelpers";

import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Dialog, DialogContent, DialogTitle} from "@mui/material";
import {
  H4,
} from "@salt-ds/core";


export const TransactionResults = (props) => {
    const { transactions, modelTypeSelected } = props;
    const [modalTransactionID, setModalTransactionID] = useState(0);
    const [modalTransactionData, setmodalTransactionData] = useState(null);
    const [modalWindowOpen, setModalWindowOpen] = useState(false);
    const { agGridProps, containerProps, api} = useAgGridHelpers();
    const gridRef = useRef(null);


    async function handleModalOpen(event, transactionID, paymentID) {
        let modalType = modelTypeSelected === " " ? "payment" : modelTypeSelected;
        let result = await importSingleTransaction(paymentID, modalType);
     
    
        setmodalTransactionData(result);
        setModalTransactionID(transactionID);
        showDetailsModal(transactionID, paymentID);
        setModalWindowOpen(true);
      }
    
      const handleModalClose = () => {
        setModalTransactionID(0);
        setmodalTransactionData(null);
        setModalWindowOpen(false)
      };
      const cellClassRules = {
        "cell-pass": params => params.value >= 75,
        "cell-avarage": params => (params.value > 50 && params.value <= 74),
        "cell-fail": params => (params.value > 0 && params.value <= 50) 
      };
      
    
      const renderCellColor= () => {
        if(transactions !== undefined && transactions.headers !== undefined){
         transactions.headers[1].children.map((item, index) => {
              if (item.field === 'confidenceScore') {
                delete item.cellClassRules;
                item.cellClassRules = cellClassRules;
                item.enableCellChangeFlash = true;
              }
            });
           
        return transactions.headers;
        }
      
      };

      useLayoutEffect(() => {
        renderCellColor();
      // setTimeout(renderCellColor, 20000);
       
        
      }, [transactions]);
      const defaultColDef = (() => {
        return {
          enableCellChangeFlash: true,
        };
      }, []);

      const showDetailsModal = (transactionID, paymentID) => {
   
        return (
          <Dialog
            open={true}
            maxWidth="lg"
            onClose={handleModalClose}
            aria-labelledby="form-dialog-title"
            className="txnDetailsWrap"
          >
            <DialogTitle id="form-dialog-title">Transaction Details</DialogTitle>
            <DialogContent>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Field</TableCell>
                      <TableCell align="right">Value</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {modalTransactionData && modalTransactionData.data.map((eachItem, index) => (
                      <TableRow key={index}>
                        <TableCell>{eachItem.label}</TableCell>
                        <TableCell align="right">{eachItem.value}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </DialogContent>
          </Dialog>
        );
     
    };
    return (
        
        <div {...containerProps} className="ag-theme-salt txn_details" style={{height:'410px' , paddingBottom:'10px 0', marginBottom: '18px'}}>
           {transactions &&  
           <>
           <H4 style={{padding: "10px 5px" }}> Transaction Details</H4>
           <AgGridReact
                {...agGridProps}
                rowData={transactions.data}
                columnDefs={transactions.headers}
                ref={gridRef}
                allowContextMenuWithControlKey
                pagination
                paginationPageSize={100}
                onRowDoubleClicked={(e) =>  handleModalOpen(
                    e,
                    e.data.id,
                    e.data.id
                )}
                defaultColDef = {defaultColDef}
                />
               </>
            }
             { modalWindowOpen && modalWindowOpen == true && showDetailsModal()} 
            </div>
                
    )
}

export default TransactionResults;