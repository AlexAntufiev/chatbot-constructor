import axios from 'axios';
import {handleRequest} from 'app/service/handleRequest'
import * as ApiPoints from "app/constants/apiPoints";
import makeTemplateStr from "app/utils/makeTemplateStr";

export function getVotesList(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeTemplateStr(ApiPoints.VOTE_LIST, {id: botSchemeId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}
