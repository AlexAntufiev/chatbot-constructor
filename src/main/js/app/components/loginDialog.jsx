import React from 'react';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {InputText} from 'primereact/inputtext';
import {Password} from 'primereact/password';
import {Growl} from 'primereact/growl';
import {BaseDialog} from 'app/components/baseDialog';
import {FormattedMessage, injectIntl} from 'react-intl';
import {AUTHORIZATION} from "app/constants/cookies";
import {connect} from "react-redux";
import setUserInfo from "app/actions/userInfo";
import * as UserService from "app/service/user"
import * as AxiosMessages from 'app/utils/axiosMessages';

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

        if (this.state.password.trim() === '' || this.state.password.trim() === '') {
            AxiosMessages.customError(this, intl.formatMessage({id: 'app.errormessage.fillallfields'}));
            return;
        }

        UserService.login(this.state.username, this.state.password, (res) => {
            this.props.setUser({
                userId: res.data.payload.userId,
                token: res.headers[AUTHORIZATION]
            });
            this.onHide();
        }, null, this);
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
                <Dialog closable={false} footer={footer} visible={this.state.visible} className="dialog"
                        modal={true} onHide={this.onHide}>
                <span className="p-float-label">
                    <InputText id="login-username" value={this.state.username}
                               onChange={(e) => this.setState({username: e.target.value})}
                               style={{overflow: 'hidden'}} onKeyDown={(e) => {if (e.key === 'Enter') this.onLogin()}}/>
                    <label htmlFor="login-username"><FormattedMessage id='app.dialog.username'/></label>
                </span>
                    <span className="p-float-label">
                    <Password id="login-password" feedback={false} value={this.state.password}
                              onChange={(e) => this.setState({password: e.target.value})}
                              onKeyDown={(e) => {if (e.key === 'Enter') this.onLogin()}}/>
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
