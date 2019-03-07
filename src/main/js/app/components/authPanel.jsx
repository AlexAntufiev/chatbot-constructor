import React from 'react';
import {connect} from 'react-redux';
import {injectIntl} from 'react-intl';
import RegisterDialog from 'app/components/registerDialog';
import LoginDialog from 'app/components/loginDialog';
import {Button} from 'primereact/button';
import {LOGOUT} from "app/constants/apiPoints";
import axios from "axios";
import setUserInfo from "app/actions/userInfo";
import {Cookies} from "react-cookie";
import {AUTHORIZATION, USER_ID} from "app/constants/cookies";

class AuthPanel extends React.Component {
    constructor(props) {
        super(props);
        this.registrationDialog = React.createRef();
        this.loginDialog = React.createRef();
        this.onShowRegistrationDialog = this.onShowRegistrationDialog.bind(this);
        this.onShowLoginDialog = this.onShowLoginDialog.bind(this);
        this.onLogout = this.onLogout.bind(this);
    }

    onShowRegistrationDialog() {
        this.registrationDialog.current.getWrappedInstance().onShow();
    }

    onShowLoginDialog() {
        this.loginDialog.current.getWrappedInstance().getWrappedInstance().onShow();
    }

    onLogout() {
        axios.post(LOGOUT).then(() => {
            const cookies = new Cookies();
            cookies.remove(AUTHORIZATION);
            cookies.remove(USER_ID);

            this.props.setUser(null);
        });
    }

    render() {
        const {intl} = this.props;
        if (this.props.isLogin) {
            return (
                <span>
                <Button label={intl.formatMessage({id: 'app.menu.logout'})} icon="pi pi-power-off"
                        onClick={this.onLogout}
                        style={{marginRight: 12}}/>
            </span>
            );
        } else {
            return (
                <span>
                <RegisterDialog ref={this.registrationDialog}/>
                <LoginDialog ref={this.loginDialog}/>
                <Button label={intl.formatMessage({id: 'app.menu.signin'})} onClick={this.onShowLoginDialog}
                        style={{marginRight: 4}}/>
                <Button label={intl.formatMessage({id: 'app.menu.signup'})} onClick={this.onShowRegistrationDialog}
                        style={{marginRight: 12}}/>
            </span>
            );
        }
    }
}
const mapDispatchToProps = dispatch => ({
    setUser: userInfo => dispatch(setUserInfo(userInfo))
});


function mapStateToProps(state) {
    return {
        userId: state.userInfo.userId,
        isLogin: state.userInfo.userId != null
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(injectIntl(AuthPanel));
