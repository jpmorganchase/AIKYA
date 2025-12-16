/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importWorklflowRunModel = (modelTypeSelected) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${modelTypeSelected}/worklflowRunModel`;

    let result = inboundFetch(communicationRoute);
    return result;
};

export default { importWorklflowRunModel };
