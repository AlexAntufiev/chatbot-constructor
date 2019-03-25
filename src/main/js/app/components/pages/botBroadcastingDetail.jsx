import React, {Component} from 'react';
import {Button} from "primereact/button";
import TextMessage from "app/components/constructor/textMessage";
import * as BroadcastMessageService from "app/service/broadcastMessage";
import * as routers from 'app/constants/routes';
import makeUrl from "app/utils/makeUrl";

export default class BotBroadcastingDetail extends Component {
    constructor(props) {
        super(props);

        this.state = {
            messageList: [],
            editedMessage: {}
        };
        this.onAddMessage = this.onAddMessage.bind(this);
        this.createMessagesList = this.createMessagesList.bind(this);
    }

    onAddMessage() {
        const defaultName = 'Broadcast message';
        BroadcastMessageService.addBroadcastMessage(this.props.match.params.id, this.props.match.params.chatChannelId,
            {title: defaultName, firingTime: 1000000000}, (res) => {
                const newMessUrl = makeUrl(routers.botBroadcastingDetailMessage(), {
                    id: this.props.match.params.id,
                    chatChannelId: this.props.match.params.chatChannelId,
                    messageId: res.data.payload.id,
                });
                let messageList = this.state.messageList;
                messageList[res.data.payload.id] = res.data.payload;
                this.setState({messageList: messageList});
                this.props.history.push(newMessUrl);
            });
    }

    componentDidMount() {
        BroadcastMessageService.getBroadcastMessages(this.props.match.params.id, this.props.match.params.chatChannelId,
            (res) => {
                let messageList = {};
                res.data.payload.forEach((message) => {
                    messageList[message.id] = message;
                });
                this.setState({messageList: messageList});
            });
    }

    createMessagesList() {
        let renderMessageList = [];
        for (let messageId in this.state.messageList) {
            const message = this.state.messageList[messageId];
            const messUrl = makeUrl(routers.botBroadcastingDetailMessage(), {
                id: this.props.match.params.id,
                chatChannelId: this.props.match.params.chatChannelId,
                messageId: messageId
            });
            renderMessageList.push(
                <div className="bot-broadcasting_elements-container_element">
                    <Button label={message.title} icon='pi pi-envelope'
                            onClick={() => this.props.history.push(messUrl)}/>
                </div>
            );
        }
        return renderMessageList;
    }

    render() {
        const channelsList = this.createMessagesList();

        const message = Number(this.props.match.params.messageId)
            ? this.state.messageList[Number(this.props.match.params.messageId)]
            : null;
        return (<div className="p-grid p-align-start bot-broadcasting">
            <div className="p-col bot-broadcasting_elements-container">
                {channelsList}
                <div className="bot-broadcasting_elements-container_element">
                    <Button label={'Добавить'} icon='pi pi-plus' onClick={this.onAddMessage}/>
                </div>
            </div>
            <div className="p-col">
                <TextMessage message={message}/>
            </div>
        </div>);
    }
}
