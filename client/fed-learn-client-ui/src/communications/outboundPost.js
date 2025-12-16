/* eslint-disable import/no-anonymous-default-export */
import axios from "axios";

export const outboundPost = async (communicationRoute, messagePayload) => {
    const communicationHeaders = {
        "Content-Type": "application/json",
        Authorization: "",
        "x-header-ip": ""
    };

    let result = "";

    try {
        result = await axios.post(communicationRoute, messagePayload, {
            headers: communicationHeaders
        });
    } catch (error) {
        console.log("There was an error with the communication", error);
        return false;
    }

    return result;
};

export default { outboundPost };
