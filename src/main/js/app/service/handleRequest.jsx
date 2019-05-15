import * as AxiosMessages from 'app/utils/axiosMessages';

export function handleRequest(request, callbackSuccess, callbackFail, context) {
    request.then((res) => {
        //check old-style response (without payload and success)
        if (typeof res.data !== "object") {
            res.data = {};
        }

        if (!('success' in res.data)) {
            res.data = {
                payload: res.data
            };
            if (res.status === 200) {
                res.data.success = true;
            } else {
                res.data.success = false;
                res.data.error = "app.errormessage.serverwrongresponse";
            }
        }

        if (res.data.success) {
            callbackSuccess && callbackSuccess(res);
        } else {
            callbackFail && callbackFail(res);
            context && AxiosMessages.serverErrorResponse(context, res.data.error);
        }

    }).catch((error) => {
        console.log(error);
        callbackFail && callbackFail(error);
        context && AxiosMessages.serverNotResponse(context);
    });
}


export function handleMultiplyRequests(request, callbackSuccess, callbackFail, context) {
    request.then((res) => {
        let allOk = true;
        res.forEach((result) => {
            //check old-style response (without payload and success)
            if (typeof result.data !== "object") {
                result.data = {};
            }

            if (!('success' in result.data)) {
                result.data = {
                    payload: result.data
                };
                if (result.status === 200) {
                    result.data.success = true;
                } else {
                    result.data.success = false;
                    result.data.error = "app.errormessage.serverwrongresponse";
                }
            }

            if (!result.data.success) {
                allOk = false;
            }
        });

        if (allOk) {
            callbackSuccess && callbackSuccess(res);
        } else {
            callbackFail && callbackFail(res);
            context && AxiosMessages.serverErrorResponse(context, "app.errormessage.serverwrongresponse");
        }
    }).catch((error) => {
        console.log(error);
        callbackFail && callbackFail(error);
        context && AxiosMessages.serverNotResponse(context);
    });
}