import axios from 'axios';
import {handleRequest} from 'app/service/handleRequest'
import * as ApiPoints from "app/constants/apiPoints";

export function getRegistrationUrl(callbackSuccess, callbackFail, context) {
    handleRequest(axios.get(ApiPoints.GET_REGISTRATION_LINK), callbackSuccess, callbackFail, context);
}
