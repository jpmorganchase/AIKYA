/* eslint-disable import/no-anonymous-default-export */
import { outboundPost } from "../communications/outboundPost";
const config = require("../config");

export const importLoadData = async (communicationPayload) => {
    let host = config.getHost();
    const communicationRoute = `${host}/loadLocalData`;

    let result = await outboundPost(communicationRoute, communicationPayload);
    return result;
};

export default { importLoadData };
