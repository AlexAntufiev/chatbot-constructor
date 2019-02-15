import * as ApiPoints from "app/constants/apiPoints";
import {handleRequest} from "app/service/handleRequest";
import axios from "axios";
import makeTemplateStr from "app/utils/makeTemplateStr";

export function getChannels(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BOT_CHANNELS_LIST, {id: botSchemeId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function getTamChatsWhereAdmin(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BOT_WHERE_ADMIN_LIST, {id: botSchemeId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function saveChannel(botSchemeId, chatId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BOT_SAVE_CHANNEL, {id: botSchemeId});
    handleRequest(axios.post(url, {chatChannel: chatId}), callbackSuccess, callbackFail, context);
}

export function deleteChannel(botSchemeId, chatId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BOT_DELETE_CHANNEL, {id: botSchemeId, chatId: chatId});
    handleRequest(axios.post(url), callbackSuccess, callbackFail, context);
}