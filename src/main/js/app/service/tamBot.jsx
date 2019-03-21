import makeUrl from "app/utils/makeUrl";
import * as ApiPoints from "app/constants/apiPoints";
import handleRequest from 'app/service/handleRequest'
import axios from 'axios';

export function getTamBot(botId, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.BOT_STATUS, {id: botId});
    handleRequest(axios.get(url), callbackSuccess, callbackFail, context);
}