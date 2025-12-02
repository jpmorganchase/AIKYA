import React from "react";

// Default behaviour for the Modal Provider Context
// i.e. if for whatever reason the consumer is used outside of a provider,
// options will be returned empty
const defaultBehaviour = {
    openNotify: false,
    showNotifyBell: false,
    setShowNotifyBell: () => undefined,
    setOpenNotify: () => undefined
};

const ModalContext = React.createContext(defaultBehaviour);

export default ModalContext;
