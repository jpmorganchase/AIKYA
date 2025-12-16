/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importGloblaModel = async (params, modelType) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${modelType}/getLatestGlobalModel?${params}`;

    let userInfo = localStorage.getItem(process.env.REACT_APP_BANK + "loggedInfo");

    const communicationHeaders = {
        headers: {
            idToken: JSON.parse(userInfo).idToken
        }
    };

    let result = await inboundFetch(communicationRoute, communicationHeaders);
    return result;
};

export default { importGloblaModel };
