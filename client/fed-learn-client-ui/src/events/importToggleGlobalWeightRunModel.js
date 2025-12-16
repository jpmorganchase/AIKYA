/* eslint-disable import/no-anonymous-default-export */
import { outboundPost } from "../communications/outboundPost";
const config = require("../config");

export const importToggleGlobalWeightRunModel = async (id, communicationPayload) => {
    let host = config.getHost();
    const communicationRoute = `${host}/toggle/globalWeightRunModel/${id}`;

    let result = await outboundPost(communicationRoute, communicationPayload);
    return result;
};

export default { importToggleGlobalWeightRunModel };
