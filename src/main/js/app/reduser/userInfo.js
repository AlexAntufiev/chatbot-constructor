import {USER_ID} from "app/constants/cookies";
import {Cookies} from "react-cookie";

const initialState = {
    userId: fetchUserId()
};

function fetchUserId() {
    const cookies = new Cookies();
    const userId = cookies.get(USER_ID);
    if (userId !== undefined) {
        return userId;
    } else {
        return null;
    }
}

export default function userInfo(state = initialState, action) {
    const newState = Object.assign({}, state);

    switch (action.type) {
        case 'SET_USER_INFO':
            newState.userId = action.payload;
            return newState;
        default:
            return newState;
    }
}
