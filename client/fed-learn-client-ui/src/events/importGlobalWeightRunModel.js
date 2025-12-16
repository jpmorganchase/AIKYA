/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importGlobalWeightRunModel = (modelTypeSelected) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${modelTypeSelected}/globalWeightRunModel`;

    let result = inboundFetch(communicationRoute);
    return result;
};

export default { importGlobalWeightRunModel };
