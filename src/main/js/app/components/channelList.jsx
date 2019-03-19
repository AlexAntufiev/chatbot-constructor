import React from "react";
import {Card} from "primereact/card";
import {Button} from "primereact/button";
import * as ChatChannelService from "app/service/chatChannel"
import {Growl} from "primereact/growl";
import {connect} from "react-redux";
import {injectIntl} from "react-intl";

class ChannelList extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            availableChannels: {},
            userChannels: {},
            ajaxRefreshProcess: false
        };
        this.refreshChannels = this.refreshChannels.bind(this);
    }

    componentDidMount() {
        const self = this;

        ChatChannelService.getChannels(this.props.botSchemeId, (res) => {
            let channels = {};
            res.data.chatChannels.forEach((channel) => {
                channel.chat_id = channel.id.chatId;
                channels[channel.id.chatId] = channel;
            });
            self.setState({userChannels: channels});
        }, null, this);
    }

    refreshChannels() {
        const self = this;
        this.setState({ajaxRefreshProcess: true});
        ChatChannelService.getTamChatsWhereAdmin(this.props.botSchemeId, (res) => {
            let channels = {};
            res.data.chatChannels.forEach((channel) => {
                if (!(channel.chat_id in self.state.userChannels)) {
                    channels[channel.chat_id] = channel;
                }
            });
            self.setState({
                availableChannels: channels,
                ajaxRefreshProcess: false
            });
        }, (res) => self.setState({ajaxRefreshProcess: false}), this);
    }

    getAvailableChannelsList() {
        const self = this;

        function append(chat) {
            const chat_id = chat.chat_id;
            ChatChannelService.saveChannel(self.props.botSchemeId, chat_id, (res) => {
                let userChannels = self.state.userChannels;
                let availableChannels = self.state.availableChannels;
                delete availableChannels[chat_id];

                userChannels[chat_id] = chat;
                self.setState({userChannels: userChannels});
                self.setState({availableChannels: availableChannels});
            }, null, self);
        }

        const {intl} = this.props;
        let renderedAvailableChannels = [];
        for (let chat_id in this.state.availableChannels) {
            const footer = (
                <span>
                    <Button label={intl.formatMessage({id: 'app.dialog.append'})} icon="pi pi-angle-up"
                            onClick={() => append(this.state.availableChannels[chat_id])}/>
                </span>
            );
            renderedAvailableChannels.push(<Card title={this.state.availableChannels[chat_id].title} footer={footer}
                                                 className={"p-col channel-list-element"}/>);
        }
        return renderedAvailableChannels;
    }

    getUserChannelsList() {
        const self = this;

        function remove(chat) {
            const chat_id = chat.chat_id;

            ChatChannelService.deleteChannel(self.props.botSchemeId, chat_id, (res) => {
                let userChannels = self.state.userChannels;
                let availableChannels = self.state.availableChannels;
                delete userChannels[chat_id];

                availableChannels[chat_id] = chat;
                self.setState({userChannels: userChannels});
                self.setState({availableChannels: availableChannels});

            }, null, self)
        }

        const {intl} = this.props;
        let renderedUserChannels = [];
        for (let chat_id in this.state.userChannels) {
            const footer = (
                <span>
                    <Button label={intl.formatMessage({id: 'app.bot.remove'})} className={'p-button-secondary'}
                            onClick={() => remove(this.state.userChannels[chat_id])} icon="pi pi-times"/>
                </span>
            );
            renderedUserChannels.push(<Card footer={footer} title={this.state.userChannels[chat_id].title}
                                            className={"p-col channel-list-element"}/>);
        }
        return renderedUserChannels;
    }

    render() {
        const availableChannels = this.getAvailableChannelsList();
        const userChannels = this.getUserChannelsList();
        const {intl} = this.props;

        return (
            <div>
                <Growl ref={(el) => this.growl = el}/>
                <div className={"p-grid"}>
                    {userChannels}
                </div>
                <Button label={intl.formatMessage({id: 'app.dialog.refresh'})} disabled={this.state.ajaxRefreshProcess}
                        className="channel-list_refresh-button" icon="pi pi-refresh" onClick={this.refreshChannels}/>
                <div className={"p-grid"}>
                    {availableChannels}
                </div>
            </div>
        );
    }
}

export default connect()(injectIntl(ChannelList));