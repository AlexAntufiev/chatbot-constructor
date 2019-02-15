import axios from 'axios';
import {handleRequest} from 'app/service/handleRequest'
import * as ApiPoints from "app/constants/apiPoints";

export function logout(callbackSuccess, callbackFail, context) {
    handleRequest(axios.post(ApiPoints.LOGOUT), callbackSuccess, callbackFail, context);
}

export function login(login, password, callbackSuccess, callbackFail, context) {
    handleRequest(axios.post(ApiPoints.LOGIN, {
        login: login,
        password: password
    }), callbackSuccess, callbackFail, context);
}
