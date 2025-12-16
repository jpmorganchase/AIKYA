/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importContributions = async () => {
    let host = config.getAggHost();
    const communicationRoute = `${host}/getContributions`;

    let result = await inboundFetch(communicationRoute);
    return result;
};

export default { importContributions };
