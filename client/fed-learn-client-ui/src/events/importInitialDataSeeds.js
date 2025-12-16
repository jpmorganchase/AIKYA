/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importInitialDataSeeds = (modelType) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${modelType}/getInitialDataSeeds`;

    let result = inboundFetch(communicationRoute);
    return result;
};

export default { importInitialDataSeeds };
