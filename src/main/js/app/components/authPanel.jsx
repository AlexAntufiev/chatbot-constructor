import React from 'react';
import {connect} from 'react-redux';
import {injectIntl} from 'react-intl';
import * as ResourcesService from 'app/service/resources';
import LoginDialog from 'app/components/loginDialog';
import {Button} from 'primereact/button';
import setUserInfo from "app/actions/userInfo";
import {Cookies} from "react-cookie";
import {AUTHORIZATION, USER_ID} from "app/constants/cookies";
import * as UserService from "app/service/user"

class AuthPanel extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            registrationLink: ''
        };
        this.registrationDialog = React.createRef();
        this.loginDialog = React.createRef();
        this.onShowLoginDialog = this.onShowLoginDialog.bind(this);
        this.onLogout = this.onLogout.bind(this);
    }

    componentDidMount() {
        ResourcesService.getRegistrationUrl((res) => {
            this.setState({registrationLink: res.data.payload.url});
        }, null, this);
    }

    onShowLoginDialog() {
        this.loginDialog.current.getWrappedInstance().getWrappedInstance().onShow();
    }

    onLogout() {
        const self = this;
        UserService.logout((res) => {
            const cookies = new Cookies();
            cookies.remove(AUTHORIZATION);
            cookies.remove(USER_ID);
            self.props.setUser({
                userId: null,
                token: null
            });
        }, null, this);
    }

    render() {
        const {intl} = this.props;
        if (this.props.isLogin) {
            return (
                <span className="auth-panel">
                    <Button label={intl.formatMessage({id: 'app.menu.logout'})} icon="pi pi-power-off"
                            onClick={this.onLogout}/>
                </span>
            );
        } else {
            return (
                <span className="auth-panel">
                    <Button label={intl.formatMessage({id: 'app.menu.signin'})} onClick={this.onShowLoginDialog}/>
                    <Button label={intl.formatMessage({id: 'app.menu.signup'})}
                            onClick={() => {
                                window.location.href = this.state.registrationLink
                            }}/>
                    <LoginDialog ref={this.loginDialog}/>
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
