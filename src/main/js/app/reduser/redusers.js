import {combineReducers} from 'redux';

import userInfo from 'app/reduser/userInfo';
import locale from 'app/reduser/locale';

export const reducers = combineReducers({
    userInfo,
    locale
});