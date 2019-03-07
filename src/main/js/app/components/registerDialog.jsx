import React from 'react';
import axios from 'axios';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {InputText} from 'primereact/inputtext';
import {Password} from 'primereact/password';
import {Growl} from 'primereact/growl';
import {BaseDialog} from 'app/components/baseDialog';
import {FormattedMessage, injectIntl} from 'react-intl';
import {apiRegistration} from "../constants/routes";
import {REGISTRATION} from "../constants/apiPoints";

class RegisterDialog extends BaseDialog {

    constructor(props) {
        super(props);
        this.state = {
            password: '',
            confirmPassword: '',
            username: ''
        };
        this.onRegistration = this.onRegistration.bind(this);
    }

    onRegistration() {
        const {intl} = this.props;
        if (this.state.password.trim() == '' || this.state.confirmPassword.trim() == '' || this.state.username.trim() == '') {
            this.growl.show({
                severity: 'error',
                summary: intl.formatMessage({id: 'app.errormessage.errorsummary'}),
                detail: intl.formatMessage({id: 'app.errormessage.fillallfields'})
            });
            return;
        }

        if (this.state.password !== this.state.confirmPassword) {
            this.growl.show({
                severity: 'error',
                summary: intl.formatMessage({id: 'app.errormessage.errorsummary'}),
                detail: intl.formatMessage({id: 'app.errormessage.passwordnotmatch'})
            });
        } else {
            axios
                .post(REGISTRATION, {login: this.state.username, password: this.state.password})
                .then(function (response) {
                    if (response.status === 200) {
                        //todo authorize user after sign up
                    }
                })
                .catch(function (error) {
                    //todo catch and process
                });
            this.onHide();
        }
    }

    render() {
        const {intl} = this.props;
        const footer = (
            <div>
                <Button label={intl.formatMessage({id: 'app.dialog.registration'})} icon="pi pi-check"
                        onClick={this.onRegistration}/>
                <Button label={intl.formatMessage({id: 'app.dialog.close'})} icon="pi pi-times" onClick={this.onHide}
                        className="p-button-secondary"/>
            </div>
        );

        return (
            <div>
                <Growl ref={(el) => this.growl = el}/>
                <Dialog closable={false} footer={footer} visible={this.state.visible} style={{width: '300px'}}
                        modal={true} onHide={this.onHide}>
                <span className="p-float-label" style={{marginTop: '20px'}}>
                    <InputText id="register-username" value={this.state.username}
                               onChange={(e) => this.setState({username: e.target.value})}
                               style={{overflow: 'hidden'}}/>
                    <label htmlFor="register-username"><FormattedMessage id='app.dialog.username'/></label>
                </span>
                    <span className="p-float-label" style={{marginTop: '20px'}}>
                    <Password id="register-password" value={this.state.password}
                              onChange={(e) => this.setState({password: e.target.value})}
                              weakLabel={intl.formatMessage({id: 'app.dialog.password.weak'})}
                              mediumLabel={intl.formatMessage({id: 'app.dialog.password.medium'})}
                              strongLabel={intl.formatMessage({id: 'app.dialog.password.strong'})}
                              promptLabel={intl.formatMessage({id: 'app.dialog.password.enter'})}/>
                    <label htmlFor="register-password"><FormattedMessage id='app.dialog.password'/></label>
                </span>
                    <span className="p-float-label" style={{marginTop: '20px'}}>
                    <Password id="register-confirm-password" feedback={false} value={this.state.confirmPassword}
                              onChange={(e) => this.setState({confirmPassword: e.target.value})}/>
                    <label htmlFor="register-confirm-password"><FormattedMessage
                        id='app.dialog.confirmpassword'/></label>
                </span>
                </Dialog>
            </div>
        );
    }
}

export default injectIntl(RegisterDialog, {withRef: true})
