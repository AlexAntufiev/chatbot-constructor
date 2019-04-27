const API_POINT = "/api/";
const CHAT_CHANNEL =  API_POINT + "bot/:id/tam/chatchannel/";

export const WEBSOCKET_URL =  window.location.origin + "/ws";
export const LOGIN = API_POINT + "login";
export const LOGOUT = API_POINT + "logout";
export const REGISTRATION = API_POINT + "registration";

export const ADD_BOT = API_POINT + "bot/add";
export const BOT_LIST = API_POINT + "bot/list";
export const DELETE_BOT = API_POINT + "bot/:id/delete";
export const BOT_INFO = API_POINT + "bot/:id";
export const SAVE_BOT = API_POINT + "bot/:id/save";
export const BOT_STATUS = API_POINT + "bot/:id/status";

export const CONNECT_BOT = API_POINT + "bot/:id/tam/connect";
export const DISCONNECT_BOT = API_POINT + "bot/:id/tam/disconnect";

export const BOT_CHANNELS_LIST = CHAT_CHANNEL + "list";
export const BOT_SAVE_CHANNEL = CHAT_CHANNEL + "save";
export const BOT_DELETE_CHANNEL = CHAT_CHANNEL + ":chatId/delete";
export const BOT_WHERE_ADMIN_LIST = CHAT_CHANNEL + "admin/list";
export const ADD_ATTACHMENT = CHAT_CHANNEL + ":chatChannelId/message/:messageId/attachment";
export const GET_ATTACHMENTS_LIST = CHAT_CHANNEL + ":chatChannelId/message/:messageId/attachment/list";
export const REMOVE_ATTACHMENT = CHAT_CHANNEL + ":chatChannelId/message/:messageId/attachment/:attachmentId/delete";

export const ADD_BROADCAST_MESSAGE = CHAT_CHANNEL + ":chatChannelId/message";
export const GET_BROADCAST_MESSAGE_LIST = CHAT_CHANNEL + ":chatChannelId/message/list";
export const BROADCAST_MESSAGE = CHAT_CHANNEL + ":chatChannelId/message/:messageId";
export const DELETE_BROADCAST_MESSAGE = CHAT_CHANNEL + ":chatChannelId/message/:messageId/delete";

export const GET_UPLOAD_ATTACHMENT_LINK = API_POINT + "bot/:id/tam/upload/:attachmentType";

const RESOURCES = '/resources/';
export const GET_REGISTRATION_LINK = RESOURCES + "registration/bot/url";
