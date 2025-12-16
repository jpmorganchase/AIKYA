/* eslint-disable import/no-unresolved */
import React from 'react';
import "react-perfect-scrollbar/dist/css/styles.css";
import { useRoutes } from "react-router-dom";
import { ThemeProvider } from "@mui/material/styles";
import GlobalStyles from "src/components/GlobalStyles";
import theme from "src/theme";
import routes from "src/routes";
import "./styles/_bootstrap.css";
import "./styles/app.css";

import "./styles/gear.css";
// import "./styles/network_app.css";

const App = () => {
    const routing = useRoutes(routes);

    return (
        <ThemeProvider theme={theme}>
            <GlobalStyles />
            {routing}
        </ThemeProvider>
    );
};

export default App;
