/* eslint-disable import/no-anonymous-default-export */
import axios from "axios";

export const inboundFetch = async (communicationRoute, headers) => {
    const communicationHeaders = {
        headers: {
            "Content-Type": "application/json",
            Authorization: "",
            "x-header-ip": ""
        }
    };
    let result = "";

    try {
        result = await axios.get(communicationRoute, headers ? headers : communicationHeaders);
    } catch (error) {
        console.log("There was an error with the communication", error);
        return false;
    }

    return result;
};

export default { inboundFetch };
