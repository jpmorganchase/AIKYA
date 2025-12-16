/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importSingleTransaction = async (paymentID, modelTypeSelected) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${modelTypeSelected}/` + paymentID;

    let result = await inboundFetch(communicationRoute);
    return result;
};

export default { importSingleTransaction };
