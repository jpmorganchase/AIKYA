/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importBatchTransactions = (batchID, modelTypeSelected) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${modelTypeSelected}/batch-grid/` + batchID;

    let result = inboundFetch(communicationRoute);
    return result;
};

export default { importBatchTransactions };
