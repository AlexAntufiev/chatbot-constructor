import * as ApiPoints from "app/constants/apiPoints";
import handleRequest from "app/service/handleRequest";
import axios from "axios";
import makeUrl from "app/utils/makeUrl";

export function getChannels(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.BOT_CHANNELS_LIST, {id: botSchemeId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function getTamChatsWhereAdmin(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.BOT_WHERE_ADMIN_LIST, {id: botSchemeId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function storeChannel(botSchemeId, chatId,  callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.BOT_STORE_CHANNEL, {id: botSchemeId});
    handleRequest(axios.post(url, {chatChannel: chatId}), callbackSuccess, callbackFail, context);
}
