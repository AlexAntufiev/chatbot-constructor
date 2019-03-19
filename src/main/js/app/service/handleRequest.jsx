import * as AxiosMessages from 'app/utils/axiosMessages';

function defaultSuccessAndFailCallback(callbackSuccess, res) {
    if (!!callbackSuccess) {
        callbackSuccess(res);
    }
}

function defaultNotResponseCallback(callbackFail, error, context) {
    if (!!callbackFail) {
        callbackFail(error);
    }
    if (!!context) {
        AxiosMessages.serverNotResponse(context)
    }
}

export default function handleRequest(request, callbackSuccess, callbackFail, context) {
    request.then((res) => {
        defaultSuccessAndFailCallback(callbackSuccess, res);
    }).catch((error) => {
        defaultNotResponseCallback(callbackFail, error, context);
    });
}
