/* eslint-disable import/no-anonymous-default-export */
import { outboundPost } from "../communications/outboundPost";
const config = require("../config");

export const importSignIn = async (communicationPayload) => {
    let host = config.getHost();
    let bank = process.env.REACT_APP_BANK;
    const communicationRoute = `${host}/signin/${bank}`;

    let result = await outboundPost(communicationRoute, communicationPayload);
    return result;
};

export default { importSignIn };
