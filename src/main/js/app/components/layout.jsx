import React, {Component} from 'react';
import {connect} from 'react-redux';
import * as routers from 'app/constants/routes'
import ApplicationRouter from 'app/router/routerSwitch';
import AuthPanel from 'app/components/authPanel';
import Locale from 'app/components/locale';
import {Menubar} from 'primereact/menubar';
import {injectIntl} from 'react-intl';
import {Cookies} from "react-cookie";
import {AUTHORIZATION, USER_ID} from "app/constants/cookies";
import setUserInfo from "app/actions/userInfo";

class IndexLayout extends Component {
    constructor(props) {
        super(props);

        const cookies = new Cookies();
        this.props.setUser({
            userId: cookies.get(USER_ID),
            token: cookies.get(AUTHORIZATION)
        });
    }

    render() {
        const {intl} = this.props;

        let menuItems = [{
            label: intl.formatMessage({id: 'app.menu.home'}),
            command: (event) => {
                this.props.history.push(routers.index())
            }
        }];
        if (this.props.isLogin) {
            menuItems.push({
                label: intl.formatMessage({id: 'app.menu.botlist'}),
                command: (event) => {
                    this.props.history.push(routers.botList())
                }
            });
        }

        return (
            <div>
                <div>
                    <Menubar model={menuItems}>
                        <div className="p-grid auth-locale-panel">
                            <AuthPanel/>
                            <Locale/>
                        </div>

                    </Menubar>
                </div>
                <ApplicationRouter/>
            </div>
        );
    }
}

const mapDispatchToProps = dispatch => ({
    setUser: userInfo => dispatch(setUserInfo(userInfo))
});

function mapStateToProps(state) {
    return {
        locale: state.locale.locale,
        userInfo: state.userInfo,
        isLogin: state.userInfo.userId != null
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(injectIntl(IndexLayout));
