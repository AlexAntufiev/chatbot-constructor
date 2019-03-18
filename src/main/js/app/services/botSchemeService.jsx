import axios from 'axios';
import * as ApiPoints from 'app/constants/apiPoints';
import makeUrl from 'app/utils/makeUrl'
import * as DefaultCallback from 'app/services/defaultCallbacks'

export function connect(botSchemeId, token, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.CONNECT_BOT, {id: botSchemeId});
    axios.post(url, {token: token}).then((res) => {
        DefaultCallback.defaultSuccessAndFailCallback(callbackSuccess, callbackFail, res);
    }).catch((error) => {
        DefaultCallback.defaultNotResponseCallback(context);
    });
}

export function disconnect(botSchemeId, callbackSuccess, callbackFail, context) {
    const url = makeUrl(ApiPoints.DISCONNECT_BOT, {id: botSchemeId});
    axios.post(url).then((res) => {
        DefaultCallback.defaultSuccessAndFailCallback(callbackSuccess, callbackFail, res);
    }).catch((error) => {
        DefaultCallback.defaultNotResponseCallback(context);
    });
}
