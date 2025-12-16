import React, { useState } from "react";
import PropTypes from "prop-types";
import ModalContext from "./ModalContext";

const ModalProvider = ({ children }) => {
    const [showNotifyBell, setShowNotifyBell] = useState(false);

    const [openNotify, setOpenNotify] = useState(false);

    return (
        <ModalContext.Provider
            value={{
                openNotify,
                showNotifyBell,
                setShowNotifyBell,
                setOpenNotify
            }}
        >
            {children}
        </ModalContext.Provider>
    );
};

ModalProvider.propTypes = {
    children: PropTypes.node
};

export default ModalProvider;
