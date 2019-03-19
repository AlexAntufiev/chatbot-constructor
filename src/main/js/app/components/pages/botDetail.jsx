import React from 'react';
import {connect} from 'react-redux';
import {Redirect} from 'react-router-dom';
import LeftMenu from 'app/components/leftMenu';
import {Route, Switch} from 'react-router';
import BotSettings from 'app/components/pages/botSettings';
import * as routes from 'app/constants/routes';
import BotConstructor from "app/components/pages/botConstructor";
import BotStatistic from "app/components/pages/botStatistic";
import BotBroadcasting from "app/components/pages/botBroadcasting";
import BotBroadcastingDetail from "app/components/pages/botBroadcastingDetail"

class BotDetail extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        if (!this.props.isLogin) {
            return (<Redirect to='/'/>);
        }
        return (
            <div className="p-grid p-align-start bot-detail-page">
                <div className="p-col left-container">
                    <div className="box"><LeftMenu id={this.props.match.params.id}/></div>
                </div>
                <div className="p-col central-container">
                    <div className="box">
                        <Switch>
                            <Route path={routes.botSettings()} component={BotSettings}/>
                            <Route path={routes.botSetup()} component={BotConstructor}/>
                            <Route path={routes.botStatistic()} component={BotStatistic}/>
                            <Route path={routes.botBroadcastingDetail()} component={BotBroadcastingDetail}/>
                            <Route path={routes.botBroadcasting()} component={BotBroadcasting}/>
                            <Redirect to={routes.botSettings()}/>
                        </Switch>
                    </div>
                </div>
            </div>
        );
    }
}

const mapStateToProps = state => ({
    userId: state.userInfo.userId,
    isLogin: state.userInfo.userId != null
});

export default connect(mapStateToProps)(BotDetail);
