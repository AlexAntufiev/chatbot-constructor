import React, {Component} from 'react';
import {Button} from "primereact/button";
import TextMessage from "app/components/constructor/textMessage";
import * as BroadcastMessageService from "app/service/broadcastMessage";
import * as routers from 'app/constants/routes';
import makeTemplateStr from "app/utils/makeTemplateStr";
import BroadcastMessageState from 'app/utils/broadcastMessageState'
import dateFormat from 'dateformat';
import {withRouter} from "react-router";
import {connect} from "react-redux";
import {injectIntl} from "react-intl";
import {Growl} from "primereact/growl";
import * as ElementId from 'app/constants/ElementId';
import * as CustomEventType from 'app/constants/CustomEventType';

class BotBroadcastingDetail extends Component {
    constructor(props) {
        super(props);

        this.state = {
            messageList: {},
            ajaxRefreshProcess: false
        };
        this.onAddMessage = this.onAddMessage.bind(this);
        this.createMessagesList = this.createMessagesList.bind(this);
        this.refreshMessageList = this.refreshMessageList.bind(this);
        this.updateMessageList = this.updateMessageList.bind(this);
    }

    onAddMessage() {
        const defaultName = 'Broadcast message';
        BroadcastMessageService.addBroadcastMessage(this.props.match.params.id, this.props.match.params.chatChannelId,
            {title: defaultName}, (res) => {
                const newMessUrl = makeTemplateStr(routers.botBroadcastingDetailMessage(), {
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

    updateMessageList(message, remove = false) {
        let messageList = Object.assign({}, this.state.messageList);
        if (!remove) {
            messageList[message.id] = message;
        } else {
            delete messageList[message.id];
        }
        this.setState({messageList: messageList});
    }

    refreshMessageList() {
        this.setState({ajaxRefreshProcess: true});
        BroadcastMessageService.getBroadcastMessages(this.props.match.params.id, this.props.match.params.chatChannelId,
            (res) => {
                let messageList = {};
                res.data.payload.forEach((message) => {
                    messageList[message.id] = message;
                });
                this.setState({
                    messageList: messageList,
                    ajaxRefreshProcess: false
                });
            }, (error) => {
                this.setState({ajaxRefreshProcess: false})
            }, this);
    }

    componentDidMount() {
        this.refreshMessageList();
        document
            .getElementById(ElementId.BOT_BROADCAST_DETAIL)
            .addEventListener(CustomEventType.BROADCAST_MESSAGE_STATE_CHANGE, (event) => {
                let messages = Object.assign({}, this.state.messageList);
                messages[event.detail.message.payload.id] = event.detail.message.payload;
                this.setState({messageList: messages});
            });
    }

    getLabelByState(message) {
        let labelText = '';
        let labelClass = '';
        const {intl} = this.props;
        switch (message.state) {
            case BroadcastMessageState.SHEDULED:
                const dateTimeFormat = this.props.locale === 'ru' ? "dd-mm-yyyy H:MM" : "dd-mm-yyyy h:MM TT";
                labelText = intl.formatMessage({id: 'app.broadcastmessage.send.wait'})
                    + ' ' + dateFormat(new Date(message.firingTime), dateTimeFormat);
                labelClass = 'pi pi-clock sheduled';
                break;
            case BroadcastMessageState.ERASED_BY_SCHEDULE:
                labelText = intl.formatMessage({id: 'app.broadcastmessage.sent.erased'});
                labelClass = 'pi pi-check sent';
                break;
            case BroadcastMessageState.SENT:
                labelText = intl.formatMessage({id: 'app.broadcastmessage.sent'});
                labelClass = 'pi pi-check sent';
                break;
            case BroadcastMessageState.ERROR:
                labelText = intl.formatMessage({id: message.error});
                labelClass = 'pi pi-info error';
                break;
            case BroadcastMessageState.PROCESSING:
                labelText = intl.formatMessage({id: 'app.broadcastmessage.processing'});
                labelClass = 'pi pi-ellipsis-h processing';
        }

        if (labelClass !== '' && labelText !== '') {
            labelClass += ' message-label';
            return (
                <span className={labelClass}>{labelText}</span>
            );
        }
        return null;
    }

    createMessagesList() {
        const dispMess = Number(this.props.match.params.messageId);

        let renderMessageList = [];
        for (let messageId in this.state.messageList) {
            const message = this.state.messageList[messageId];
            const messUrl = makeTemplateStr(routers.botBroadcastingDetailMessage(), {
                id: this.props.match.params.id,
                chatChannelId: this.props.match.params.chatChannelId,
                messageId: messageId
            });
            renderMessageList.push(
                <div className="bot-broadcasting_elements-container_element">
                    <div>
                        {this.getLabelByState(message)}
                    </div>
                    <Button label={message.title} icon='pi pi-envelope'
                            className={dispMess && Number(messageId) === dispMess ? "broadcasting-message selected" : "broadcasting-message"}
                            onClick={() => {
                                this.props.history.push(messUrl);
                            }}/>
                </div>
            );
        }
        return renderMessageList;
    }

    render() {
        const messagesList = this.createMessagesList();
        const {intl} = this.props;
        const message = Number(this.props.match.params.messageId)
            ? this.state.messageList[Number(this.props.match.params.messageId)]
            : null;
        return (<div className="p-grid p-align-start bot-broadcasting" id={ElementId.BOT_BROADCAST_DETAIL}>
            <Growl ref={(el) => this.growl = el}/>
            <div className="p-col bot-broadcasting_elements-container">
                <div className="bot-broadcasting_elements-container_element">
                    <Button label={intl.formatMessage({id: 'app.dialog.refresh'})}
                            icon={"pi pi-refresh" + (this.state.ajaxRefreshProcess ? " pi-spin" : "")}
                            disabled={this.state.ajaxRefreshProcess}
                            onClick={this.refreshMessageList}/>
                </div>
                {messagesList}
                <div className="bot-broadcasting_elements-container_element">
                    <Button label={intl.formatMessage({id: 'app.dialog.append'})} icon='pi pi-plus'
                            onClick={this.onAddMessage}/>
                </div>
            </div>
            <div className="p-col">
                <TextMessage botSchemeId={this.props.match.params.id}
                             botId={this.props.match.params.id}
                             chatChannelId={this.props.match.params.chatChannelId}
                             updateMessageList={this.updateMessageList}
                             message={message}/>
            </div>
        </div>);
    }
}

const mapStateToProps = state => ({
    locale: state.locale.locale
});

export default withRouter(connect(mapStateToProps)(injectIntl(BotBroadcastingDetail)));
