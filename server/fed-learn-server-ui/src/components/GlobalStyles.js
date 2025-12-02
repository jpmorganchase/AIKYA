import { makeStyles } from "@mui/styles";

const useStyles = makeStyles({
    "@global": {
        "*": {
            boxSizing: "border-box",
            margin: 0,
            padding: 0
        },
        html: {
            "-webkit-font-smoothing": "antialiased",
            "-moz-osx-font-smoothing": "grayscale",
            height: "100%",
            width: "100%"
        },
        body: {
            backgroundColor: "#f4f6f8",
            height: "100%",
            width: "100%"
        },
        a: {
            textDecoration: "none"
        },
        "#root": {
            height: "100%",
            width: "100%"
        },
        ".fixed-cells": {
            backgroundColor: "#f5f5f5 !important",
            color: "#999 !important"
        },
        "th.fixed-cells": {
            backgroundColor: "#f5f5f5 !important",
            color: "#000 !important"
        },
        ".data-rows:hover": {
            backgroundColor: "#f8f8ff !important",
            color: "#000 !important"
        },
        ".data-rows:hover .fixed-cells": {
            backgroundColor: "#f8f8ff !important",
            color: "#000 !important"
        }
    }
});

const GlobalStyles = () => {
    useStyles();

    return null;
};

export default GlobalStyles;
