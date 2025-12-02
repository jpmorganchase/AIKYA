import React from "react";

const Logo = ({ userDetails }) => {
    return (
        <h3>
            <font color="white" style={{ fontFamily: "Roboto" }}>
                {userDetails && userDetails.user.company}
            </font>
        </h3>
    );
};

export default Logo;
