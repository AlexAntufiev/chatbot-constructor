import React, {Component} from 'react';
import {InputText} from 'primereact/inputtext';
import {Button} from 'primereact/button';
import {Growl} from "primereact/growl";
import * as AxiosMessages from 'app/utils/axiosMessages';
import {connect} from "react-redux";
import {FormattedMessage, injectIntl} from "react-intl";
import ChannelList from "app/components/channelList";
import * as BotSchemeService from "app/service/botScheme"
import * as TamBotService from "app/service/tamBot"

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

    refreshToken() {
        const self = this;
        TamBotService.getTamBot(this.props.match.params.id, (res) => {
            if (self.state.connected) {
                self.setState({token: res.data.token});
            }
        }, null, this)
    }

    refreshBotInfo() {
        const self = this;
        BotSchemeService.getBotScheme(this.props.match.params.id, (res) => {
            const connected = !!res.data.botId;
            self.setState({
                name: res.data.name,
                botId: res.data.botId,
                connected: connected,
                initialName: res.data.name,
            });
            if (connected) {
                self.refreshToken(res.data.botId);
            }
        }, null, this);
    }

    onConnectButtonBot() {
        this.setState({connectAjaxProcess: true});
        const self = this;

        function callbackSuccess(res) {
            self.setState({connectAjaxProcess: false});
            let successMessId;
            if (self.state.connected) {
                successMessId = 'success.tam.bot.unsubscribed';
            } else {
                successMessId = 'success.tam.bot.subscribed';
            }
            self.setState({connected: !self.state.connected});
            AxiosMessages.successOperation(self, successMessId);
        }

        if (this.state.connected) {
            BotSchemeService.disconnect(this.props.match.params.id, callbackSuccess, () => {
                self.setState({connectAjaxProcess: false});
            }, this);
        } else {
            BotSchemeService.connect(this.props.match.params.id, this.state.token, callbackSuccess, () => {
                self.setState({connectAjaxProcess: false});
            }, this);
        }
    }

    onSaveBot() {
        this.setState({saveAjaxProcess: true});
        const self = this;

        BotSchemeService.saveBot(this.props.match.params.id, self.state.name,
            (res) => {
                self.setState({saveAjaxProcess: false});
                self.setState({initialName: self.state.name});
                AxiosMessages.successOperation(self, 'success.tam.bot.name.changed');
            }, (error) => {
                self.setState({saveAjaxProcess: false});
            }, this);
    }

    render() {
        const {intl} = this.props;

        const saveDisabled = this.state.name === this.state.initialName
            || this.state.name.trim() === ''
            || this.state.saveAjaxProcess;
        const connectDisabled = !this.state.connected && this.state.token.trim() === ''
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
                {this.state.connected &&
                <div>
                    <h3><FormattedMessage id='app.bot.detail.select.channels'/></h3>
                    <ChannelList botSchemeId={this.props.match.params.id}/>
                </div>
                }
            </div>);
    }
}

export default connect()(injectIntl(BotSettings));
