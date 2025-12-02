/* eslint-disable import/no-anonymous-default-export */
import { outboundPost } from "../communications/outboundPost";
const config = require("../config");

export const exportTransactions = async (payload, batchID) => {
    let host = config.getHost();
    const communicationRoute = `${host}/sendFeedback`;
    const communicationPayload = { feedbacks: payload, batchId:batchID };

    // console.log("Transaction Exported", communicationPayload);

    let result = await outboundPost(communicationRoute, communicationPayload);
    return result;
};

export default { exportTransactions };
