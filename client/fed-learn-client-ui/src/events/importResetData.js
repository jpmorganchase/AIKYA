/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importResetData = async (modelType) => {
    let host = config.getHost();
    const communicationRoute = `${host}/reset`;

    let result = await inboundFetch(communicationRoute, {});
    return result;
};

export default { importResetData };
