/* eslint-disable import/no-unresolved */
import React from 'react';
import { Navigate } from "react-router-dom";
import DashboardLayout from "./components/DashboardLayout";
import MainLayout from "./components/MainLayout";

import Dashboard from "./views/Dashboard";
import NotFound from "./views/NotFound";
import Login from "./views/Login";
// import MetaData from "./pages/MetaData";


const routes = [
    {
        path: "signin",
        element: <Login />
    },
    {
        path: "app",
        element: <DashboardLayout />,
        children: [
            // { path: "transactions", element: <TransactionsList /> },
       
            { path: "dashboard", element: <Dashboard /> },
            { path: "*", element: <Navigate to="/404" /> }
        ]
    },
    {
        path: "/",
        element: <MainLayout />,
        children: [
            { path: "404", element: <NotFound /> },
            { path: "/", element: <Navigate to="/signin" /> },
            { path: "*", element: <Navigate to="/404" /> }
        ]
    }
];

export default routes;
