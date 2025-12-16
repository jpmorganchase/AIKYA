/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importGlobalSummaryChart = async (modelType) => {
    let host = config.getAggHost();
    const communicationRoute = `${host}/${modelType}/performances`;

    let result = await inboundFetch(communicationRoute);
    return result;
};

export default { importGlobalSummaryChart };
