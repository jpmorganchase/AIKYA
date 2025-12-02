
import React from 'react';
import ReactDOM from "react-dom";
import reportWebVitals from './reportWebVitals';
import App from "./App";
import { MemoryRouter } from "react-router-dom";
import ModalProvider from "./components/ModalProvider/ModalProvider";


const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <MemoryRouter>
        <ModalProvider>
          <App />
        </ModalProvider>
    </MemoryRouter>
);



reportWebVitals();

