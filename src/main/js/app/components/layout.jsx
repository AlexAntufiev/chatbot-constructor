import React, {Component} from 'react';
import {connect} from 'react-redux';
import * as routers from 'app/constants/routes'
import ApplicationRouter from 'app/router/routerSwitch';
import AuthPanel from 'app/components/authPanel';
import Locale from 'app/components/locale';
import {Menubar} from 'primereact/menubar';
import {injectIntl} from 'react-intl';
import {Cookies} from "react-cookie";
import {AUTHORIZATION} from "app/constants/cookies";
import axios from 'axios';

class IndexLayout extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        const cookies = new Cookies();
        axios.defaults.headers.common[AUTHORIZATION] = cookies.get(AUTHORIZATION);
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
                        <div style={{display: 'flex'}}>
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

function mapStateToProps(state) {
    return {
        userId: state.userInfo.userId,
        isLogin: state.userInfo.userId != null
    };
}

export default connect(mapStateToProps)(injectIntl(IndexLayout));
