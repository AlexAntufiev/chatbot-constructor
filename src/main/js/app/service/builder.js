import axios from 'axios';
import * as ApiPoints from 'app/constants/apiPoints';
import makeTemplateStr from 'app/utils/makeTemplateStr';
import {handleRequest, handleMultiplyRequests} from 'app/service/handleRequest';

export function newComponent(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BUILDER_NEW_COMPONENT, {id: botSchemeId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function getBotScheme(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BUILDER_SCHEME, {id: botSchemeId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function saveBotScheme(botSchemeId, components, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BUILDER_SCHEME, {id: botSchemeId});
    handleRequest(axios.post(url, components), callbackSuccess, callbackFail, context);
}
