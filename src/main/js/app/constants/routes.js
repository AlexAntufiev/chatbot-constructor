// Get public path
const publicPath = process.env.ASSET_PATH;

// Index route
export const index = () => '/';
// Bot list route
export const botList = () => '/bot/';

export const botDetail = () => '/bot/:id/';

export const botSettings = () => '/bot/:id/settings/';
export const botSetup = () => '/bot/:id/setup/';
export const botStatistic = () => '/bot/:id/statistic/';
export const botBroadcasting = () => '/bot/:id/broadcasting/';
export const botBroadcastingDetail = () => '/bot/:id/broadcasting/:chatChannelId/';
export const botBroadcastingDetailMessage = () => '/bot/:id/broadcasting/:chatChannelId/message/:messageId';
