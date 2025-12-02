/* eslint-disable import/no-anonymous-default-export */
import { outboundPost } from "../communications/outboundPost";
const config = require("../config");

export const importSendGlobalModelAggregate = async (id, communicationPayload) => {
    let host = config.getHost();
    const communicationRoute = `${host}/sendGlobalModelAggregate/${id}`;

    let result = await outboundPost(communicationRoute, communicationPayload);
    return result;
};

export default { importSendGlobalModelAggregate };