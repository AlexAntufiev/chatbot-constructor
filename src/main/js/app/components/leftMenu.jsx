import React, {Component} from 'react';
import {Menu} from 'primereact/menu';
import {connect} from "react-redux";
import {injectIntl} from "react-intl";
import * as routers from 'app/constants/routes';
import makeTemplateStr from 'app/utils/makeTemplateStr';
import {withRouter} from "react-router";

class LeftMenu extends Component {
    constructor(props) {
        super(props);
        const {intl} = this.props;
        const settingsUrl = makeTemplateStr(routers.botSettings(), {id: this.props.id});
        const constructorUrl = makeTemplateStr(routers.botSetup(), {id: this.props.id});
        const statisticUrl = makeTemplateStr(routers.botStatistic(), {id: this.props.id});
        const broadcastingUrl = makeTemplateStr(routers.botBroadcasting(), {id: this.props.id});
        this.state = {
            items: [
                {
                    label: intl.formatMessage({id: 'app.menu.settings'}),
                    icon: 'pi pi-fw pi-cog',
                    command: () => {
                        this.props.history.push(settingsUrl)
                    }
                },
                {
                    label: intl.formatMessage({id: 'app.menu.constructor'}),
                    icon: 'pi pi-table',
                    command: () => {
                        this.props.history.push(constructorUrl)
                    }
                },
                {
                    label: intl.formatMessage({id: 'app.menu.broadcasting'}),
                    icon: 'pi pi-envelope',
                    command: () => {
                        this.props.history.push(broadcastingUrl)
                    }
                },
                {
                    label: intl.formatMessage({id: 'app.menu.statistic'}),
                    icon: 'pi pi-chart-bar',
                    command: () => {
                        this.props.history.push(statisticUrl)
                    }
                },
            ]
        }
    }

    render() {

        return (
            <Menu model={this.state.items} className="left-menu"/>
        );
    }
}

export default withRouter(connect()(injectIntl(LeftMenu)));
