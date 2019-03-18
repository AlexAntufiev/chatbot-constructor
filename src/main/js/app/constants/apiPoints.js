const API_POINT = "/api/";

export const LOGIN = API_POINT + "login";
export const LOGOUT = API_POINT + "logout";
export const REGISTRATION = API_POINT + "registration";

export const ADD_BOT = API_POINT + "bot/add";
export const BOT_LIST = API_POINT + "bot/list";
export const DELETE_BOT = API_POINT + "bot/delete";
export const BOT_INFO = API_POINT + "bot/:id";
export const SAVE_BOT = API_POINT + "bot/:id/save";
export const BOT_STATUS = API_POINT + "bot/:id/status";

export const CONNECT_BOT = API_POINT + "bot/:id/tam/connect";
export const DISCONNECT_BOT = API_POINT + "bot/:id/tam/disconnect";
