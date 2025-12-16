// import { useState } from "react";
import React, { useEffect, useState } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import styled from 'styled-components';
import Header from "./Header";

import { importUser } from "../events/importUser";
// import DashboardSidebar from "./DashboardSidebar";

const DashboardLayoutRoot = styled("div")(({ theme }) => ({
    display: "flex",
    overflow: "hidden",
    width: "100%"
}));

const DashboardLayoutWrapper = styled("div")(({ theme }) => ({
    display: "flex",
    flex: "1 1 auto",
    overflow: "hidden",
    padding: '53px 20px 20px 25px'
    // [theme.breakpoints.up("lg")]: {
    //     paddingLeft: 150
    // }
}));

const DashboardLayoutContainer = styled("div")({
    display: "flex",
    flex: "1 1 auto",
    overflow: "hidden"
});

const DashboardLayoutContent = styled("div")({
    flex: "1 1 auto",
    height: "100%",
});

const DashboardLayout = () => {
    // const [isMobileNavOpen, setMobileNavOpen] = useState(false);

    const [userDetails, setUserDetails] = useState();
    const navigate = useNavigate();

    useEffect(() => {
        fetchUser();
    }, []);

    async function fetchUser() {
        try {
            let user = await importUser();
            if (user.data && (user.data.statusCode === 403 || user.data.statusCode === 404)) {
                localStorage.removeItem(process.env.REACT_APP_BANK + "loggedInfo");
                navigate("/");
            } else if (typeof user === "object") {
                setUserDetails(user.data);
            } else {
                localStorage.removeItem(process.env.REACT_APP_BANK + "loggedInfo");
                navigate("/");
            }
        } catch (e) {
            console.log("There was an error importing the statistics");
            localStorage.removeItem(process.env.REACT_APP_BANK + "loggedInfo");
            navigate("/");
        }
    }

    return (
        <DashboardLayoutRoot>
            <Header userDetails={userDetails} />
            {/* <onMobileNavOpen={() => setMobileNavOpen(true)} /> */}
            {/* <DashboardSidebar onMobileClose={() => setMobileNavOpen(false)} openMobile={isMobileNavOpen} /> */}
            <DashboardLayoutWrapper>
                <DashboardLayoutContainer>
                    <DashboardLayoutContent>
                        <Outlet context={[userDetails]} />
                    </DashboardLayoutContent>
                </DashboardLayoutContainer>
            </DashboardLayoutWrapper>
        </DashboardLayoutRoot>
    );
};

export default DashboardLayout;
