/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importContributions = async (modelType) => {
    let host = config.getAggHost();
    const communicationRoute = `${host}/${modelType}/contributions`;

    let result = await inboundFetch(communicationRoute);
    return result;
};

export default { importContributions };
