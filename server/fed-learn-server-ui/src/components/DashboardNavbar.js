import React from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { Fragment, useContext } from "react";
import PropTypes from "prop-types";
import { AppBar, Badge, Box, IconButton, Toolbar, Typography } from "@mui/material";

import LoggedInIcon from "@mui/icons-material/Person";
import InputIcon from "@mui/icons-material/Input";
import Logo from "./Logo";
import ModalContext from "./ModalProvider/ModalContext";
import NotificationsActiveIcon from "@mui/icons-material/NotificationsActive";
import { makeStyles } from "@mui/styles";

const useStyles = makeStyles((theme) => ({
    root: {
        "&.bank1": {
            backgroundColor: "#87CEFA !important"
        },
        "&.bank2": {
            backgroundColor: "#DEB887 !important"
        },
        "&.bank3": {
            backgroundColor: "#20B2AA !important"
        },
        "&.network": {
            backgroundColor: "#9099ca !important"
        },
        color: "#000 !important",
        "& font": {
            color: "#000 !important"
        },
        "& button": {
            padding: "5px"
        },
        "& h3": {
            marginBottom: "0 !important"
        }
    },
    toolbar: {
        minHeight: "40px !important"
    }
}));

const DashboardNavbar = ({ onMobileNavOpen, userDetails, ...rest }) => {
    const classes = useStyles();

    const navigate = useNavigate();

    const { showNotifyBell, setOpenNotify } = useContext(ModalContext);

    const onLogout = () => {
        localStorage.removeItem(process.env.REACT_APP_BANK + "loggedInfo");
        navigate("/");
    };

    const onNotifyClick = () => {
        setOpenNotify(true);
    };

    function UserDetails() {
        return (
            <Fragment>
                <Typography
                    color="textLight"
                    variant="h6"
                    sx={{
                        textTransform: "uppercase",
                        fontSize: "1rem"
                    }}
                >
                    {userDetails && userDetails.user ? userDetails.user.name : ""}
                </Typography>
                &nbsp;
                <IconButton color="inherit">
                    <Badge variant="dot">
                        <LoggedInIcon />
                    </Badge>
                </IconButton>
            </Fragment>
        );
    }

    function NotifyBellDetails() {
        return (
            <Fragment>
                <IconButton color="inherit" onClick={onNotifyClick}>
                    <Badge variant="dot">
                        <NotificationsActiveIcon style={{ color: "red"}} />
                    </Badge>
                </IconButton>
            </Fragment>
        );
    }

    if (!userDetails) {
        return <></>;
    }

    return (
        <AppBar elevation={0} {...rest} className={`${classes.root} ${process.env.REACT_APP_BANK}`}>
            <Toolbar className={classes.toolbar}>
                <RouterLink to="/">
                    <Logo userDetails={userDetails} />
                </RouterLink>
                <Box sx={{ flexGrow: 1 }} />
                <UserDetails></UserDetails>
                {showNotifyBell && <NotifyBellDetails></NotifyBellDetails>}
                <IconButton color="inherit" onClick={onLogout} title="Logout">
                    <InputIcon />
                </IconButton>
            </Toolbar>
        </AppBar>
    );
};

DashboardNavbar.propTypes = {
    onMobileNavOpen: PropTypes.func
};

export default DashboardNavbar;
