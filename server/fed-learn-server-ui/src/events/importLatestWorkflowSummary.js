/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importLatestWorkflowSummary = async (modelType) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${modelType}/latestWorkflowSummary`;

    let result = await inboundFetch(communicationRoute);
    return result;
};

export default { importLatestWorkflowSummary };
