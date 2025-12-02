/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importSummaryChart = async (modelType) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${modelType}/getSummaryChart`;

    let result = await inboundFetch(communicationRoute);
    return result;
};

export default { importSummaryChart };
