import axios from 'axios';
import * as ApiPoints from 'app/constants/apiPoints';
import makeTemplateStr from 'app/utils/makeTemplateStr';
import {handleRequest} from 'app/service/handleRequest';

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

export function getGroups(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BUILDER_SCHEME_GROUPS_LIST, {id: botSchemeId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}

export function addGroup(botSchemeId, title, type, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BUILDER_SCHEME_GROUP_ADD, {id: botSchemeId});
    handleRequest(axios.post(url, {
        schemeId: botSchemeId,
        title: title,
        type: type
    }), callbackSuccess, callbackFail, context);
}

export function updateGroup(id, botSchemeId, title, type, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BUILDER_SCHEME_GROUP, {id: botSchemeId});
    handleRequest(axios.post(url, {
        schemeId: botSchemeId,
        title: title,
        type: type,
        id: id
    }), callbackSuccess, callbackFail, context);
}

export function removeGroup(botSchemeId, groupId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.BUILDER_SCHEME_GROUP_REMOVE, {id: botSchemeId, groupId: groupId});
    handleRequest(axios.post(url), callbackSuccess, callbackFail, context);
}
