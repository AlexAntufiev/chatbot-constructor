import axios from 'axios';
import * as ApiPoints from 'app/constants/apiPoints';
import makeUrl from 'app/utils/makeUrl'
import handleRequest from 'app/service/handleRequest'

export function addBroadcastMessage(botSchemeId, chatChannelId, broadcastMessage, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.ADD_BROADCAST_MESSAGE, {id: botSchemeId, chatChannelId: chatChannelId});
    handleRequest(axios.post(url, broadcastMessage), callbackSuccess, callbackFail, context);
}

export function getBroadcastMessage(botSchemeId, chatChannelId, messageId, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.BROADCAST_MESSAGE, {
        id: botSchemeId,
        chatChannelId: chatChannelId,
        messageId: messageId
    });
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function getBroadcastMessages(botSchemeId, chatChannelId, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.GET_BROADCAST_MESSAGE_LIST, {id: botSchemeId, chatChannelId: chatChannelId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function removeBroadcastMessage(botSchemeId, chatChannelId, messageId, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.DELETE_BROADCAST_MESSAGE, {
        id: botSchemeId,
        chatChannelId: chatChannelId,
        messageId: messageId
    });
    handleRequest(axios.post(url), callbackSuccess, callbackFail, context);
}

export function updateBroadcastMessage(botSchemeId, chatChannelId, messageId, message, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.BROADCAST_MESSAGE, {
        id: botSchemeId,
        chatChannelId: chatChannelId,
        messageId: messageId
    });
    handleRequest(axios.post(url, message), callbackSuccess, callbackFail, context);
}

export function addAttachment(botSchemeId, chatChannelId, messageId, attachment, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.ADD_ATTACHMENT, {
        id: botSchemeId,
        chatChannelId: chatChannelId,
        messageId: messageId
    });
    handleRequest(axios.post(url, attachment), callbackSuccess, callbackFail, context);
}

export function getAttacmentsList(botSchemeId, chatChannelId, messageId, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.GET_ATTACHMENTS_LIST, {
        id: botSchemeId,
        chatChannelId: chatChannelId,
        messageId: messageId
    });
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function removeAttachment(botSchemeId, chatChannelId, messageId, attachmentId, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.REMOVE_ATTACHMENT, {
        id: botSchemeId,
        chatChannelId: chatChannelId,
        messageId: messageId,
        attachmentId: attachmentId
    });
    handleRequest(axios.post(url), callbackSuccess, callbackFail, context);

}
