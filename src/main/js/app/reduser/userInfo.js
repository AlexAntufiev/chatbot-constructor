import {USER_ID, AUTHORIZATION} from "app/constants/cookies";
import {Cookies} from "react-cookie";
import axios from "axios";

const initialState = fetchInitialState();

function fetchInitialState() {
    const cookies = new Cookies();
    const userId = cookies.get(USER_ID);
    const token = cookies.get(AUTHORIZATION);
    if (userId !== undefined) {
        return {userId: userId, token: token};
    } else {
        return {userId: null, token: null};
    }
}

export default function userInfo(state = initialState, action) {
    const newState = Object.assign({}, state);

    switch (action.type) {
        case 'SET_USER_INFO':
            newState.userId = action.payload.userId === undefined ? null : action.payload.userId;
            newState.token = action.payload.token === undefined ? null : action.payload.token;
            const cookies = new Cookies();
            if (newState.userId == null) {
                cookies.remove(AUTHORIZATION);
                cookies.remove(USER_ID);
                delete axios.defaults.headers.common[AUTHORIZATION];
            } else {
                cookies.set(AUTHORIZATION, action.payload.token);
                cookies.set(USER_ID, action.payload.userId);
                axios.defaults.headers.common[AUTHORIZATION] = action.payload.token;
            }
            return newState;
        default:
            return newState;
    }
}
