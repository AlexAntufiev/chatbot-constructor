import makeTemplateStr from "app/utils/makeTemplateStr";
import * as ApiPoints from "app/constants/apiPoints";
import {handleRequest} from 'app/service/handleRequest'
import axios from 'axios';

export function getTamBot(botId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BOT_STATUS, {id: botId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function getAttachmentUploadLink(botId, attachmentType, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.GET_UPLOAD_ATTACHMENT_LINK, {id:botId, attachmentType: attachmentType});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}