import * as AxiosMessages from 'app/utils/axiosMessages';

export default function handleRequest(request, callbackSuccess, callbackFail, context) {
    request.then((res) => {
        if (typeof res.data !== "object") {
            res.data = {};
        }
        if (!('success' in res.data)) {
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
        context && AxiosMessages.serverNotResponse(context)
    });
}
