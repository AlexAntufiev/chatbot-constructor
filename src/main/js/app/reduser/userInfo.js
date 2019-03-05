const initialState = {
    userId: null
};

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
