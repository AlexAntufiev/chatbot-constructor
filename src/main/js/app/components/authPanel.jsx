import React from 'react';
import {connect} from 'react-redux';
import {injectIntl} from 'react-intl';
import RegisterDialog from 'app/components/registerDialog';
import LoginDialog from 'app/components/loginDialog';
import {Button} from 'primereact/button';

class AuthPanel extends React.Component {
    constructor(props) {
        super(props);
        this.registrationDialog = React.createRef();
        this.loginDialog = React.createRef();
        this.onShowRegistrationDialog = this.onShowRegistrationDialog.bind(this);
        this.onShowLoginDialog = this.onShowLoginDialog.bind(this);
    }

    onShowRegistrationDialog() {
        this.registrationDialog.current.getWrappedInstance().onShow();
    }

    onShowLoginDialog() {
        this.loginDialog.current.getWrappedInstance().getWrappedInstance().onShow();
    }

    render() {
        const {intl} = this.props;
        if (this.props.isLogin) {
            return (
                <span>
                <Button label={intl.formatMessage({id: 'app.menu.logout'})} icon="pi pi-power-off"
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

function mapStateToProps(state) {
    return {
        userId: state.userInfo.userId,
        isLogin: state.userInfo.userId != null
    };
}

export default connect(mapStateToProps)(injectIntl(AuthPanel));
