import React from 'react';
import {connect} from 'react-redux';
import {Redirect} from 'react-router-dom';
import LeftMenu from 'app/components/leftMenu';
import {Route, Switch} from 'react-router';
import BotSettings from 'app/components/botSettings';
import * as routes from 'app/constants/routes';
import BotConstructor from "app/components/botConstructor";
import BotStatistic from "app/components/botStatistic";

class BotDetail extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        if (!this.props.isLogin) {
            return (<Redirect to='/'/>);
        }
        return (
            <div className="p-grid p-align-start p-align-stretch" style={{marginTop: '6px', flexWrap: 'nowrap'}}>
                <div className="p-col" style={{flexBasis: '15%'}}>
                    <div className="box"><LeftMenu id={this.props.match.params.id} history={this.props.history}/></div>
                </div>
                <div className="p-col" style={{flexBasis: '60%'}}>
                    <div className="box">
                        <Switch>
                            <Route path={routes.botSettings()} component={BotSettings}/>
                            <Route path={routes.botSetup()} component={BotConstructor}/>
                            <Route path={routes.botStatistic()} component={BotStatistic}/>
                            <Redirect to={routes.botSettings()}/>
                        </Switch>
                    </div>
                </div>
                <div className="p-col" style={{flexBasis: '15%'}}>
                    <div className="box"></div>
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
