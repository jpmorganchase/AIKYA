/* eslint-disable import/no-anonymous-default-export */
import { outboundPost } from "../communications/outboundPost";
const config = require("../config");

var todayDate = new Date();
var lastDate = new Date();

todayDate = todayDate.toISOString().slice(0, 10);
lastDate.setDate(lastDate.getDate() - 7);
lastDate = lastDate.toISOString().slice(0, 10);

export const importProgressStatistics = async () => {
    let host = config.getHost();
    const communicationRoute = `${host}/txnTaskProgressStat`;
    const communicationPayload = {
        from: lastDate,
        to: todayDate
    };

    let result = await outboundPost(communicationRoute, communicationPayload);
    return result;
};

export default { importProgressStatistics };
