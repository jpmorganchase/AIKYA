import { inboundFetch } from "../communications/inboundFetch";
const config = require("../config");

export const importShaplyData= (batchID, modelTypeSelected) => {
    let host = config.getHost();
    const communicationRoute = `${host}/${modelTypeSelected}/shap-values/` + batchID;

    let result = inboundFetch(communicationRoute);
    return result;
};

export default { importShaplyData };