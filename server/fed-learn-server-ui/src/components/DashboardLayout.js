// import { useState } from "react";
import React from "react";
import { Outlet, useNavigate } from "react-router-dom";
import { styled } from "@mui/material/styles";
import DashboardNavbar from "./DashboardNavbar";
import { useEffect, useState } from "react";
import { importUser } from "src/events/importUser";

const DashboardLayoutRoot = styled("div")(({ theme }) => ({
    backgroundColor: theme.palette.background.default,
    display: "flex",
    height: "100%",
    overflow: "hidden",
    width: "100%"
}));

const DashboardLayoutWrapper = styled("div")(({ theme }) => ({
    display: "flex",
    flex: "1 1 auto",
    overflow: "hidden",
    paddingTop: 38
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
    overflow: "auto"
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
            }

            if (user.data && user.data.user) {
                if (
                    process.env.REACT_APP_BANK === "network" ||
                    user.data.user.company.toLowerCase().indexOf("network") !== -1
                ) {
                    navigate("/app/networkview");
                } else {
                    navigate("/app/dashboard");
                }
            }
        } catch (e) {
            console.log("There was an error importing the statistics");
            localStorage.removeItem(process.env.REACT_APP_BANK + "loggedInfo");
            navigate("/");
        }
    }

    return (
        <DashboardLayoutRoot>
            <DashboardNavbar userDetails={userDetails} />
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
