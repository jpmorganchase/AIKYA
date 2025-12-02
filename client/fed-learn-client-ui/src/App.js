
import React from "react";
import { useRoutes } from "react-router-dom";
import routes from "./routes";
import { SaltProvider } from "@salt-ds/core";

import "@salt-ds/theme/index.css";
import "./styles/_bootstrap.css";
import "@fontsource/roboto";
import "./styles/app.css";

const App = () => {
  const routing = useRoutes(routes);

  return <SaltProvider>{routing}</SaltProvider>;
};

export default App;

