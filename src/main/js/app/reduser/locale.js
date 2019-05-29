const initialState = {locale: 'ru'};

export default function userInfo(state = initialState, action) {
    const newState = Object.assign({}, state);
    switch (action.type) {
        case 'SET_LOCALE':
            newState.locale = action.payload;
            return newState;
        default:
            return newState;
    }
}
