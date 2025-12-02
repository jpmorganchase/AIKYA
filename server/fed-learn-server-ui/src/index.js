import React from "react";
import ReactDOM from "react-dom/client";
import * as serviceWorker from "./serviceWorker";
import App from "./App";
import ModalProvider from "./components/ModalProvider/ModalProvider";
import { MemoryRouter } from "react-router-dom";

const root = ReactDOM.createRoot(document.getElementById("root"));

root.render(
    <MemoryRouter>
        <ModalProvider>
            <App />
        </ModalProvider>
    </MemoryRouter>
);

serviceWorker.unregister();
