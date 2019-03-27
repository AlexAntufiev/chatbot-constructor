import {InputText} from "primereact/inputtext";
import {Calendar} from "primereact/calendar";
import {InputTextarea} from "primereact/inputtextarea";
import {Button} from "primereact/button";
import React from "react";
import * as BroadcastMessageService from "app/service/broadcastMessage";
import {Growl} from "primereact/growl";
import {connect} from "react-redux";
import {injectIntl} from "react-intl";
import * as AxiosMessages from 'app/utils/axiosMessages';
import * as Routes from 'app/constants/routes';
import makeUrl from "app/utils/makeUrl";
import {withRouter} from "react-router";
import BroadcastMessageState from 'app/utils/broadcastMessageState';
import shallowequal from "shallowequal";
import getCalendar from "app/i18n/calendarLocale";

class TextMessage extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            title: "",
            firingTime: null,
            erasingTime: null,
            text: "",
            ajaxUpdateProcess: false,
            ajaxRemoveProcess: false
        };
        this.onUpdateMessage = this.onUpdateMessage.bind(this);
        this.onRemoveMessage = this.onRemoveMessage.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        const message = this.props.message;
        if (message && !shallowequal(prevProps.message, message)) {
            this.setState({
                title: message.title,
                firingTime: message.firingTime ? new Date(message.firingTime) : null,
                erasingTime: message.erasingTime ? new Date(message.erasingTime) : null,
                text: message.text ? message.text : "",
                ajaxUpdateProcess: false,
                ajaxRemoveProcess: false
            });
        }
    }

    onUpdateMessage() {
        if (this.state.title.trim() === '') {
            AxiosMessages.serverErrorResponse(this, 'errors.broadcast.message.title.is.empty');
            return;
        }

        if (this.props.message.state === BroadcastMessageState.CREATED && this.state.text.trim() && !this.state.firingTime) {
            AxiosMessages.serverErrorResponse(this, 'error.broadcast.message.need.fill.text.and.posttime');
            return;
        }

        this.setState({ajaxUpdateProcess: true});
        BroadcastMessageService.updateBroadcastMessage(this.props.botSchemeId, this.props.chatChannelId, this.props.message.id, {
                title: this.state.title,
                text: this.state.text,
                firingTime: this.state.firingTime ? this.state.firingTime.toUTCString() : null,
                erasingTime: this.state.erasingTime ? this.state.erasingTime.toUTCString() : null,
            },
            () => {
                this.props.refreshMessageList();
                AxiosMessages.successOperation(this, 'app.broadcastmessage.saved');
                this.setState({ajaxUpdateProcess: false});
            },
            () => this.setState({ajaxUpdateProcess: false}),
            this);
    }

    onRemoveMessage() {
        const {intl} = this.props;
        if (confirm(intl.formatMessage({id: 'app.dialog.checksure'}))) {
            this.setState({ajaxRemoveProcess: true});
            BroadcastMessageService.removeBroadcastMessage(this.props.botSchemeId, this.props.chatChannelId, this.props.message.id,
                () => {
                    const url = makeUrl(Routes.botBroadcastingDetail(), {
                        id: this.props.botId,
                        botSchemeId: this.props.botSchemeId,
                        chatChannelId: this.props.chatChannelId
                    });
                    this.props.refreshMessageList();
                    AxiosMessages.successOperation(this, 'success.tam.bot.name.changed');
                    this.setState({ajaxRemoveProcess: false});
                    this.props.history.push(url);
                }, () => this.setState({ajaxRemoveProcess: false}),
                this
            );
        }
    }

    render() {
        if (!this.props.message) {
            return (<div/>);
        }
        const {intl} = this.props;
        return (
            <div className='text-card'>
                <Growl ref={(el) => this.growl = el}/>
                <div className="text-card_detail-element">
                    <InputText placeholder={intl.formatMessage({id: 'app.dialog.name'})} value={this.state.title}
                               onChange={(e) => this.setState({title: e.target.value})}/>
                </div>
                <div className="text-card_detail-element">
                    <Calendar showTime={true} showSeconds={false}
                              placeholder={intl.formatMessage({id: 'app.broadcastmessage.postingtime'})}
                              disabled={this.props.message.state === BroadcastMessageState.SENT}
                              onChange={(e) => this.setState({firingTime: e.value})}
                              dateFormat={"D, d M yy"}
                              hourFormat={this.props.locale === 'ru' ? "24" : "12"}
                              value={this.state.firingTime}
                              locale={getCalendar(this.props.locale)}/>
                </div>
                <div className="text-card_detail-element">
                    <Calendar showTime={true} showSeconds={false}
                              placeholder={intl.formatMessage({id: 'app.broadcastmessage.erasingtime'})}
                              onChange={(e) => this.setState({erasingTime: e.value})}
                              dateFormat={"D, d M yy"}
                              hourFormat={this.props.locale === 'ru' ? "24" : "12"}
                              value={this.state.erasingTime}
                              locale={getCalendar(this.props.locale)}/>
                </div>
                <div className="text-card_detail-element">
                    <InputTextarea placeholder={intl.formatMessage({id: 'app.broadcastmessage.text'})}
                                   value={this.state.text} rows={5} cols={60}
                                   disabled={this.props.message.state === BroadcastMessageState.SENT}
                                   onChange={(e) => this.setState({text: e.target.value})}
                                   autoResize={true}
                                   maxLength={3000}/>
                </div>
                <div className="text-card_button-panel">
                    <Button
                        label={intl.formatMessage({id: 'app.dialog.save'})}
                        onClick={this.onUpdateMessage}
                        disabled={this.state.ajaxUpdateProcess}/>
                    <Button label={intl.formatMessage({id: 'app.bot.remove'})} onClick={this.onRemoveMessage}
                            disabled={this.state.ajaxRemoveProcess}/>
                </div>
            </div>
        );
    }
}

const mapStateToProps = state => ({
    locale: state.locale.locale
});

export default withRouter(connect(mapStateToProps)(injectIntl(TextMessage)));
