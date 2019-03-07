import React from 'react';
import axios from 'axios';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {InputText} from 'primereact/inputtext';
import {Password} from 'primereact/password';
import {Growl} from 'primereact/growl';
import {BaseDialog} from 'app/components/baseDialog';
import {FormattedMessage, injectIntl} from 'react-intl';
import {apiLogin} from "../constants/routes";
import {AUTHORIZATION, USER_ID} from "../constants/cookies";
import {LOGIN} from "../constants/apiPoints";
import {Cookies} from "react-cookie";
import {connect} from "react-redux";
import setUserInfo from "../actions/userInfo";

export class LoginDialog extends BaseDialog {
    constructor(props) {
        super(props);
        this.state = {
            password: '',
            username: ''
        };

        this.onLogin = this.onLogin.bind(this);
    }

    onLogin() {
        const {intl} = this.props;

        if (this.state.password.trim() == '' || this.state.password.trim() == '') {
            this.growl.show({
                severity: 'error',
                summary: intl.formatMessage({id: 'app.errormessage.errorsummary'}),
                detail: intl.formatMessage({id: 'app.errormessage.fillallfields'})
            });
        }
        axios
            .post(LOGIN, {login: this.state.username, password: this.state.password})
            .then(response => {
                const cookies = new Cookies();
                cookies.set(AUTHORIZATION, response.headers[AUTHORIZATION]);
                cookies.set(USER_ID, response.data.userId);
                this.props.setUser(response.data);
                this.onHide();
            })
            .catch(error => {
                //todo catch and process
            });
    }

    render() {
        const {intl} = this.props;

        const footer = (
            <div>
                <Button label={intl.formatMessage({id: 'app.dialog.login'})} icon="pi pi-check" onClick={this.onLogin}/>
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
                    <InputText id="login-username" value={this.state.username}
                               onChange={(e) => this.setState({username: e.target.value})}
                               style={{overflow: 'hidden'}}/>
                    <label htmlFor="login-username"><FormattedMessage id='app.dialog.username'/></label>
                </span>
                    <span className="p-float-label" style={{marginTop: '20px'}}>
                    <Password id="login-password" feedback={false} value={this.state.password}
                              onChange={(e) => this.setState({password: e.target.value})}/>
                    <label htmlFor="login-password"><FormattedMessage id='app.dialog.password'/></label>
                </span>
                </Dialog>
            </div>
        );
    }
}

const mapStateToProps = state => ({
    user: state.userInfo
});

const mapDispatchToProps = dispatch => ({
    setUser: userInfo => dispatch(setUserInfo(userInfo))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps,
    null,
    {withRef: true})(injectIntl(LoginDialog, {withRef: true}));
