import React, {Component} from 'react';
import {Growl} from "primereact/growl";
import {Button} from "primereact/button";
import {Card} from "primereact/card";
import {withRouter} from "react-router";
import {connect} from "react-redux";
import {injectIntl} from "react-intl";
import * as routes from 'app/constants/routes';
import makeUrl from 'app/utils/makeUrl';
import * as ChatChannelService from 'app/service/chatChannel';
import ChatChannelCard from "../chatChannelCard";

class BotBroadcasting extends Component {
    constructor(props) {
        super(props);

        this.state = {
            chatChannels: []
        };

        this.onChannelClick = this.onChannelClick.bind(this);
        this.getChatChannelsList = this.getChatChannelsList.bind(this);
    }

    componentDidMount() {
        const self = this;
        ChatChannelService.getChannels(this.props.match.params.id, (res) => {
            let chatChannels = [];
            res.data.payload.forEach((chatChannel) => {
               chatChannels.push({
                   chat_id: chatChannel.id.chatId,
                   title: chatChannel.title,
                   description: chatChannel.description,
                   icon: chatChannel.iconUrl,
                   link: chatChannel.link
               });
            });
            self.setState({chatChannels: chatChannels});
        }, null, this);
    }

    onChannelClick(chatChannelId) {
        const url = makeUrl(routes.botBroadcastingDetail(), {
            id: this.props.match.params.id,
            chatChannelId: chatChannelId
        });

        this.props.history.push(url);
    }

    getChatChannelsList() {
        return this.state.chatChannels.map((channel) => {
            const {intl} = this.props;

            const footer = (
                <Button label={intl.formatMessage({id: 'app.bot.edit'})} icon="pi pi-pencil"
                        className={"p-col"} onClick={() => this.onChannelClick(channel.chat_id)}/>
            );

            return (
                <ChatChannelCard chatChannel={channel} footer={footer}/>
            );
        })
    }

    render() {
        const botList = this.getChatChannelsList();

        return (
            <div className="p-grid p-align-start bot-broadcasting_list">
                <Growl ref={(el) => this.growl = el}/>
                {botList}
            </div>);
    }
}

export default withRouter(connect()(injectIntl(BotBroadcasting)));
