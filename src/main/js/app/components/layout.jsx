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
import SockJsClient from 'react-stomp';
import * as ElementId from 'app/constants/ElementId';
import * as CustomEventType from 'app/constants/CustomEventType';
import {Growl} from "primereact/growl";
import BroadcastMessageState from "app/utils/broadcastMessageState";
import makeTemplateStr from "app/utils/makeTemplateStr";
import * as AxiosMessages from "app/utils/axiosMessages";
import * as ApiPoints from 'app/constants/apiPoints';

class IndexLayout extends Component {
    constructor(props) {
        super(props);

        const cookies = new Cookies();
        this.props.setUser({
            userId: cookies.get(USER_ID),
            token: cookies.get(AUTHORIZATION)
        });
    }

    onMessage(msg) {
        switch (msg.type) {
            case CustomEventType.BROADCAST_MESSAGE_STATE_CHANGE_TYPE:
                const event = new CustomEvent(CustomEventType.BROADCAST_MESSAGE_STATE_CHANGE, {
                    detail: {
                        message: msg
                    }
                });

                const name = event.detail.message.payload.title;
                const {intl} = this.props;
                let mess;
                switch (event.detail.message.payload.state) {
                    case BroadcastMessageState.SENT:
                        mess = makeTemplateStr(intl.formatMessage({id: 'template.message.sent'}), {name: name});
                        AxiosMessages.customSuccess(this, mess);
                        break;
                    case BroadcastMessageState.ERASED_BY_SCHEDULE:
                        mess = makeTemplateStr(intl.formatMessage({id: 'template.message.erase'}), {name: name});
                        AxiosMessages.customSuccess(this, mess);
                        break;
                    case BroadcastMessageState.ERROR:
                        mess = makeTemplateStr(intl.formatMessage({id: 'template.message.error'}), {name: name});
                        AxiosMessages.customError(this, mess);
                        break;
                }
                const botBroadcastingDetail = document.getElementById(ElementId.BOT_BROADCAST_DETAIL);
                if (botBroadcastingDetail) {
                    botBroadcastingDetail.dispatchEvent(event);
                }
                break;
        }
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
        {/* @todo #CC-134 use single growl component*/}
        return (
            <div>
                {this.props.isLogin && <SockJsClient
                    url={ApiPoints.WEBSOCKET_URL}
                    topics={['/user/queue/updates']}
                    onMessage={(msg) => this.onMessage(msg)}
                    ref={(client) => {
                        this.clientRef = client
                    }}/>}
                <Growl ref={(el) => this.growl = el}/>
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
