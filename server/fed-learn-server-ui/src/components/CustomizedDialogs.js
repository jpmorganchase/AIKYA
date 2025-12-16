import React from "react";
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    MenuItem,
    Select,
    Typography
} from "@mui/material";
import { styled } from "@mui/material/styles";
import { Close as CloseIcon } from "@mui/icons-material";
import { Fragment, useEffect, useState } from "react";
// const config = require("../config");

const BootstrapDialog = styled(Dialog)(({ theme }) => ({
    "& .MuiDialogContent-root": {
        height: "100px",
        width: "450px",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        padding: theme.spacing(2)
    },
    "& .MuiDialogActions-root": {
        padding: theme.spacing(1)
    }
}));

const LoadBootstrapDialog = styled(Dialog)(({ theme }) => ({
    "& .MuiDialogContent-root": {
        height: "200px",
        width: "450px",
        padding: theme.spacing(2)
    },
    "& .MuiDialogActions-root": {
        padding: theme.spacing(1)
    }
}));

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

    const handleChange = (event) => {
        let result = event.target.value;
        setItemSelected(result);
    };

    const handleDropDownClose = () => {
        setSelected(false);
    };

    const handleDropDownOpen = () => {
        setSelected(true);
    };

    const renderCustomDialog = () => {
        return (
            <Fragment>
                <BootstrapDialog onClose={handleClose} aria-labelledby="customized-dialog-title" open={open}>
                    <DialogTitle sx={{ m: 0, p: 2 }} id="customized-dialog-title">
                        {dialogContent.title}
                    </DialogTitle>
                    <IconButton
                        aria-label="close"
                        onClick={handleClose}
                        sx={{
                            position: "absolute",
                            right: 8,
                            top: 8,
                            color: (theme) => theme.palette.grey[500]
                        }}
                    >
                        <CloseIcon />
                    </IconButton>
                    <DialogContent dividers>
                        <Typography gutterBottom>{dialogContent.desc}</Typography>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleClose}>CANCEL</Button>
                        <Button autoFocus onClick={onProceedEvt}>
                            PROCEED
                        </Button>
                    </DialogActions>
                </BootstrapDialog>
            </Fragment>
        );
    };

    const renderLoadDialog = () => {
        // const loadTypes = [{ label: "Init Load", value: `data_init_${config.getNodeNo()}` }];

        return (
            <Fragment>
                <LoadBootstrapDialog onClose={handleClose} aria-labelledby="customized-dialog-title" open={open}>
                    <DialogTitle sx={{ m: 0, p: 2 }} id="customized-dialog-title">
                        {dialogContent.title}
                    </DialogTitle>
                    <IconButton
                        aria-label="close"
                        onClick={handleClose}
                        sx={{
                            position: "absolute",
                            right: 8,
                            top: 8,
                            color: (theme) => theme.palette.grey[500]
                        }}
                    >
                        <CloseIcon />
                    </IconButton>
                    <DialogContent dividers>
                        <div className="dialog-conten">
                            <Typography gutterBottom>{dialogContent.desc}</Typography>
                        </div>
                        <Select
                            style={{ width: `100%`, border: 0, marginTop: "1rem", padding: 0 }}
                            open={isSelected}
                            onClose={handleDropDownClose}
                            onOpen={handleDropDownOpen}
                            onChange={handleChange}
                            value={itemSelected}
                        >
                            {dataSeeds &&
                                dataSeeds.map(function (item, i) {
                                    return (
                                        <MenuItem value={item.fileName} key={i}>
                                            {item.label}
                                        </MenuItem>
                                    );
                                })}
                        </Select>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleClose}>CANCEL</Button>
                        <Button autoFocus onClick={onProceedEvt}>
                            PROCEED
                        </Button>
                    </DialogActions>
                </LoadBootstrapDialog>
            </Fragment>
        );
    };

    if (dialogContent && dialogContent.id === 1) {
        return renderLoadDialog();
    }

    return renderCustomDialog();
}
