/* eslint-disable import/no-anonymous-default-export */
import { outboundPost } from "../communications/outboundPost";
const config = require("../config");

export const importRetrain = async (endpPoint, batchId) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${endpPoint}/${batchId}`;
    const communicationPayload = "";

    let result = await outboundPost(communicationRoute, communicationPayload);
    return result;
};

export default { importRetrain };
