import React, {Component} from 'react';
import {InputText} from 'primereact/inputtext';
import axios from 'axios';
import makeUrl from 'app/utils/makeUrl'
import * as ApiPoints from 'app/constants/apiPoints';
import {Button} from 'primereact/button';
import {Growl} from "primereact/growl";
import * as AxiosMessages from 'app/utils/axiosMessages';
import {connect} from "react-redux";
import {FormattedMessage, injectIntl} from "react-intl";

class BotSettings extends Component {
    // @todo #CC-19 show info about connected tam bot (name / icon)
    constructor(props) {
        super(props);

        this.state = {
            name: '',
            token: '',
            connected: false,
            botId: '',
            initialName: '',
            saveAjaxProcess: false,
            connectAjaxProcess: false,
        };
        this.onSaveBot = this.onSaveBot.bind(this);
        this.refreshBotInfo = this.refreshBotInfo.bind(this);
        this.refreshToken = this.refreshToken.bind(this);
        this.onConnectButtonBot = this.onConnectButtonBot.bind(this);
    }

    componentDidMount() {
        this.refreshBotInfo();
    }

    refreshToken(botId) {
        const url = makeUrl(ApiPoints.BOT_STATUS, {id: botId});
        axios.get(url).then((res) => {
            if (this.state.connected) {
                this.setState({token: res.data.token});
            } else {
                AxiosMessages.serverErrorResponse(this, res.data.error);
            }
        }).catch(() => AxiosMessages.serverNotResponse(this));
    }

    refreshBotInfo() {
        const url = makeUrl(ApiPoints.BOT_INFO, {id: this.props.match.params.id});
        axios.get(url).then((res) => {
            if (res.status === 200) {
                const connected = !!res.data.botId;
                this.setState({
                    name: res.data.name,
                    botId: res.data.botId,
                    connected: connected,
                    initialName: res.data.name,
                });
                if (connected) {
                    this.refreshToken(res.data.botId);
                }
            } else {
                AxiosMessages.serverErrorResponse(this);
            }
        }).catch(() => AxiosMessages.serverNotResponse(this));
    }

    onConnectButtonBot() {
        this.setState({connectAjaxProcess: true});

        let url;
        let data = {};
        let successMessId;
        if (this.state.connected) {
            successMessId = 'success.tam.bot.unsubscribed';
            url = makeUrl(ApiPoints.DISCONNECT_BOT, {id: this.props.match.params.id});
        } else {
            successMessId = 'success.tam.bot.subscribed';
            url = makeUrl(ApiPoints.CONNECT_BOT, {id: this.props.match.params.id});
            data = {token: this.state.token};
        }
        axios.post(url, data).then(res => {
            this.setState({connectAjaxProcess: false});
            if (res.data.success) {
                this.setState({connected: !this.state.connected});
                AxiosMessages.successOperation(this, successMessId);
            } else {
                AxiosMessages.serverErrorResponse(this, res.data.error);
            }
        }).catch(() => {
            this.setState({connectAjaxProcess: false});
            AxiosMessages.serverNotResponse(this);
        });
    }

    onSaveBot() {
        this.setState({saveAjaxProcess: true});
        const url = makeUrl(ApiPoints.SAVE_BOT, {id: this.props.match.params.id});
        axios.post(url, {name: this.state.name}).then((res) => {
            this.setState({saveAjaxProcess: false});
            if (res.status === 200) {
                AxiosMessages.successOperation(this, 'success.tam.bot.name.changed');
                this.setState({initialName: this.state.name,});
            } else {
                AxiosMessages.serverErrorResponse(this);
            }
        }).catch((error) => {
            this.setState({saveAjaxProcess: false});
            AxiosMessages.serverNotResponse(this)
        });
    }

    render() {
        const {intl} = this.props;

        const saveDisabled = this.state.name === this.state.initialName
            || this.state.name.trim() === ''
            || this.state.saveAjaxProcess;
        const connectDisabled = !this.state.connected  && this.state.token.trim() === ''
            || this.state.connectAjaxProcess;

        let connectButtonLabel;
        if (this.state.connected) {
            connectButtonLabel = intl.formatMessage({id: 'app.dialog.disconnect'});
        } else {
            connectButtonLabel = intl.formatMessage({id: 'app.dialog.connect'});
        }

        return (
            <div className="bot-settings-container">
                <Growl ref={(el) => this.growl = el}/>
                <div>
                    <span className="p-float-label">
                        <InputText id="bot-settings-name" value={this.state.name}
                                   onChange={(e) => this.setState({name: e.target.value})}/>
                        <label htmlFor="bot-settings-name"><FormattedMessage id='app.dialog.name'/></label>
                    </span>
                    <Button label={intl.formatMessage({id: 'app.dialog.save'})} disabled={saveDisabled}
                            icon="pi pi-check"
                            onClick={this.onSaveBot}/>
                </div>
                <div className="bot-settings-container_connect-form">
                    <span className="p-float-label">
                        <InputText id="bot-settings-token" value={this.state.token} disabled={this.state.connected}
                                   onChange={(e) => this.setState({token: e.target.value})}/>
                        <label htmlFor="bot-settings-token"><FormattedMessage id='app.dialog.token'/></label>
                    </span>
                    <Button label={connectButtonLabel} disabled={connectDisabled} icon="pi pi-wifi"
                            onClick={this.onConnectButtonBot}/>
                </div>
            </div>);
    }
}

export default connect()(injectIntl(BotSettings));
