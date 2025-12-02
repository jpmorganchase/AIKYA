import React from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { Fragment, useContext } from "react";
import PropTypes from "prop-types";
import { Badge, Button, Avatar, Text } from "@salt-ds/core";
import { NotificationSolidIcon } from "@salt-ds/icons";
import LoginIcon from '@mui/icons-material/Login';

import Logo from "../Logo";

import styled from 'styled-components';

const HeaderWrapper = styled.div`
  width: 100%;
  position:fixed;
  left:0;
  top:0;
  background:#000E33;   
  color:#fff;
  padding: 9px;
  z-index: 1001;
`;

const HeaderBlock = styled.header`
 display:flex;
 padding: 0 15px;
`;

const DisplaySpace = styled.div`
 display:flex;
 flex-grow:1;
`;



const Header = ({ onMobileNavOpen, userDetails, ...rest }) => {
  

    const navigate = useNavigate();

  

    const onLogout = () => {
        localStorage.removeItem(process.env.REACT_APP_BANK + "loggedInfo");
        navigate("/");
    };

  
    function UserDetails() {
        return (
            <Fragment>
                <h6
                    style={{
                        textTransform: "uppercase",
                        fontSize: "1.3rem",
                        lineHeight: "28px",
                        padding: "0 5px",
                    }}
                >
                    {userDetails && userDetails.user ? userDetails.user.name : ""}
                </h6>
                &nbsp;
              
                   
                    <Avatar name={userDetails.user.name} size={1} src={""} />
                    &nbsp;  &nbsp;
            </Fragment>
        );
    }

  

    if (!userDetails) {
        return <></>;
    }

    return (
        <>
        <HeaderWrapper>
            <HeaderBlock>
           
                    <Logo userDetails={userDetails} />
                
                <DisplaySpace></DisplaySpace>
                <UserDetails></UserDetails>
          
                <LoginIcon color="inherit" onClick={onLogout} title="Logout" style={{height:'2rem'}}>
                </LoginIcon>

            </HeaderBlock>
        </HeaderWrapper>
        </>
    );
};

Header.propTypes = {
    onMobileNavOpen: PropTypes.func
};

export default Header;
