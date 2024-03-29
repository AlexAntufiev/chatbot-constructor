import axios from 'axios';
import * as ApiPoints from 'app/constants/apiPoints';
import makeTemplateStr from 'app/utils/makeTemplateStr'
import {handleRequest} from 'app/service/handleRequest'

export function connect(botSchemeId, token, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.CONNECT_BOT, {id: botSchemeId});
    handleRequest(axios.post(url, {token: token}), callbackSuccess, callbackFail, context);
}

export function disconnect(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.DISCONNECT_BOT, {id: botSchemeId});
    handleRequest(axios.post(url), callbackSuccess, callbackFail, context);
}

export function saveBot(botSchemeId, botId, name, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.SAVE_BOT, {id: botSchemeId});
    handleRequest(axios.post(url, {name: name, botId: botId}), callbackSuccess, callbackFail, context);
}

export function getBotScheme(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BOT_INFO, {id: botSchemeId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function addBot(name, callbackSuccess, callbackFail, context) {
    const url = ApiPoints.ADD_BOT;
    handleRequest(axios.post(url, {name: name}), callbackSuccess, callbackFail, context);
}

export function getList(callbackSuccess, callbackFail, context) {
    const url = ApiPoints.BOT_LIST;
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function removeBot(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.DELETE_BOT, {id: botSchemeId});
    handleRequest(axios.post(url), callbackSuccess, callbackFail, context);
}
