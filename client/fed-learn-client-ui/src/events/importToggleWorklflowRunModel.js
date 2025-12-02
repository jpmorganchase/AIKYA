/* eslint-disable import/no-anonymous-default-export */
import { outboundPost } from "../communications/outboundPost";
const config = require("../config");

export const importToggleWorklflowRunModel = async (id, communicationPayload) => {
    let host = config.getHost();
    const communicationRoute = `${host}/toggle/worklflowRunModel/${id}`;

    let result = await outboundPost(communicationRoute, communicationPayload);
    return result;
};

export default { importToggleWorklflowRunModel };
