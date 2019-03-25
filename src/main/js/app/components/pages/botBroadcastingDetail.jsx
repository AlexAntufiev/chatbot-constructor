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
            messageList: []
        };
        this.onAddMessage = this.onAddMessage.bind(this);
        this.createMessagesList = this.createMessagesList.bind(this);
    }

    onAddMessage() {
        const self = this;
        const defaultName = 'Broadcast message';
        BroadcastMessageService.addBroadcastMessage(this.props.match.params.id, this.props.match.params.chatChannelId,
            {title: defaultName}, (res) => {
                const newMessUrl = makeUrl(routers.botBroadcastingDetailMessage(), {
                    id: self.props.match.params.id,
                    chatChannelId: self.props.match.params.chatChannelId,
                    messageId: res.data.payload.id
                });
                self.props.history.push(newMessUrl);
            });
    }

    componentDidMount() {
        const self = this;
        BroadcastMessageService.getBroadcastMessages(this.props.match.params.id, this.props.match.params.chatChannelId,
            (res) => {
                self.setState({messageList: res.data.payload});
            });
    }

    createMessagesList() {
        return this.state.messageList.map((message) => {

            return (
                <div className="bot-broadcasting_elements-container_element">
                    <Button label={message.title} icon='pi pi-envelope'/>
                </div>
            );
        });
    }

    render() {
        const channelsList = this.createMessagesList();

        return (<div className="p-grid p-align-start bot-broadcasting">
            <div className="p-col bot-broadcasting_elements-container">
                {channelsList}
                <div className="bot-broadcasting_elements-container_element">
                    <Button label={'Добавить'} icon='pi pi-plus' onClick={this.onAddMessage}/>
                </div>
            </div>
            <div className="p-col">
                <TextMessage/>
            </div>
        </div>);
    }
}
