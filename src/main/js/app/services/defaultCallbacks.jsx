import * as AxiosMessages from 'app/utils/axiosMessages';

export function defaultSuccessAndFailCallback(callbackSuccess, callbackFail, res) {
    if (res.status === 200) {
        if (!!callbackSuccess) {
            callbackSuccess(res);
        }
    } else {
        if (!!callbackFail) {
            callbackFail(res);
        }
    }
}

export function defaultNotResponseCallback(context) {
    if (!!context) {
        AxiosMessages.serverNotResponse(context)
    }
}