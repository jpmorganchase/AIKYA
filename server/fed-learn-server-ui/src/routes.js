/* eslint-disable import/no-unresolved */
import React from "react";
import { Navigate } from "react-router-dom";
import DashboardLayout from "src/components/DashboardLayout";
import NotFound from "src/pages/NotFound";
import Login from "./pages/Login";
import NetworkView from "./pages/NetworkView";
import Home from "./pages/Home";
import Landing from 'src/pages/Landing';

const routes = [
    {
        path: "landing",
        element: <Landing />
    },
    {
        path: "/signin",
        element: <Login />
    },
    {
        path: "app",
        element: <DashboardLayout />,
        children: [
            { path: "networkview", element: <NetworkView /> },
            { path: "home", element: <Home /> },
            { path: "*", element: <Navigate to="/404" /> }
        ]
    },
    {
        path: "/",
        element: <Landing />,
        children: [
            { path: "404", element: <NotFound /> },
            { path: "/", element: <Navigate to="/signin" /> },
            { path: "/*", element: <Navigate to="/signin" /> },
            { path: "*", element: <Navigate to="/404" /> }
        ]
    }
];

export default routes;
