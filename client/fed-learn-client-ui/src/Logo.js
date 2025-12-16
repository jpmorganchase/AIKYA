import React from "react";

import logo from './logo-kinexys-light.png';

const Logo = ({ userDetails }) => {
    return (
        <>
    {/* <div className="aikya-logo">
        <img src={logo}/>
    </div> */}
        <h3 style={{ paddingTop: "3px",  paddingLeft: "8px",  }}>
            <font color="white" style={{ fontFamily: "Roboto" }}>
                {userDetails && userDetails.user.company}
            </font>
        </h3>
        </>
    );
};

export default Logo;