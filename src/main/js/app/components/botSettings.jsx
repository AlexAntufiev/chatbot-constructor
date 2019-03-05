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
    constructor(props) {
        super(props);
        this.state = {
            name: '',
            token: '',
            initialName: '',
            initialToken: ''
        };
        this.onSaveBot = this.onSaveBot.bind(this);
        this.refreshBotInfo = this.refreshBotInfo.bind(this);
        this.onResetBot = this.onResetBot.bind(this);
    }

    refreshBotInfo() {
        const url = makeUrl(ApiPoints.BOT_INFO, {id: this.props.match.params.id});
        axios.get(url).then((res) => {
            if (res.status === 200) {
                this.setState({
                    name: res.data.name,
                    token: res.data.token,
                    initialName: res.data.name,
                    initialToken: res.data.token
                });
            } else {
                AxiosMessages.serverErrorResponse(this);
            }
        }).catch(() => AxiosMessages.serverNotResponse(this));
    }

    componentDidMount() {
        this.refreshBotInfo();
    }

    onResetBot() {
        this.refreshBotInfo();
    }

    onSaveBot() {
        if (this.state.name === this.state.initialName && this.state.token === this.state.initialToken) {
            return false;
        }

        const url = makeUrl(ApiPoints.SAVE_BOT, {id: this.props.match.params.id});
        axios.post(url, {
            name: this.state.name,
            token: this.state.token
        }).then((res) => {
            if (res.status === 200) {
                AxiosMessages.successOperation(this);
                this.setState({
                    initialName: this.state.name,
                    initialToken: this.state.token
                });
            } else {
                AxiosMessages.serverErrorResponse(this);
            }
        }).catch((error) => {
                AxiosMessages.serverNotResponse(this)
            }
        );
        return true;
    }

    render() {
        const {intl} = this.props;
        return (
            <div style={{marginTop: '6px'}}>
                <Growl ref={(el) => this.growl = el}/>
                <span className="p-float-label">
                    <InputText id="in" value={this.state.name} onChange={(e) => this.setState({name: e.target.value})}/>
                    <label htmlFor="in"><FormattedMessage id='app.dialog.name'/></label>
                </span>
                <span className="p-float-label" style={{marginTop: '18px'}}>
                    <InputText id="in" value={this.state.token}
                               onChange={(e) => this.setState({token: e.target.value})}/>
                    <label htmlFor="in"><FormattedMessage id='app.dialog.token'/></label>
                </span>
                <div style={{marginTop: '12px'}}>
                    <Button label={intl.formatMessage({id: 'app.dialog.save'})} icon="pi pi-check"
                            style={{marginRight: '6px'}} onClick={this.onSaveBot}/>
                    <Button label={intl.formatMessage({id: 'app.dialog.cancel'})} icon="pi pi-times"
                            onClick={this.onResetBot}/>
                </div>
            </div>);
    }
}

export default connect()(injectIntl(BotSettings));
