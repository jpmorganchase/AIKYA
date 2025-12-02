/* eslint-disable import/no-anonymous-default-export */
import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importBatches = async (modelType, fromDate, tooDate) => {
    let host = config.getHost();
    let communicationRoute;
    if(fromDate !== null && fromDate != undefined){
        communicationRoute = `${host}/${modelType}/dashboard/search?start=${fromDate}&end=${tooDate}`;
    }else{
        communicationRoute = `${host}/${modelType}/dashboard`;
    }
    

    let result = await inboundFetch(communicationRoute);
    return result;
};

export default { importBatches };
