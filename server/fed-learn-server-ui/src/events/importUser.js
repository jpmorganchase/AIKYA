/* eslint-disable import/no-anonymous-default-export */
import { outboundPost } from "src/communications/outboundPost";
const config = require("../config");

export const importUser = async () => {
    let host = config.getAPIHost();
    const communicationRoute = `${host}/getuser`;
    let userInfo = localStorage.getItem(process.env.REACT_APP_BANK + "loggedInfo");
    const communicationPayload = { idToken: JSON.parse(userInfo).idToken };

    let result = await outboundPost(communicationRoute, communicationPayload);
    return result;
};

export default { importUser };
