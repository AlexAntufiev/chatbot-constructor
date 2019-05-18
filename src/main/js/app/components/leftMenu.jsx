import React, {Component} from 'react';
import {Menu} from 'primereact/menu';
import {connect} from "react-redux";
import {injectIntl} from "react-intl";
import * as routers from 'app/constants/routes';
import makeTemplateStr from 'app/utils/makeTemplateStr';
import {withRouter} from "react-router";

class LeftMenu extends Component {

    static get MENU_ELEM() {
        return {
            SETTINGS: 0,
            CONSTRUCTOR: 1,
            VOTES: 2,
            BROADCASTING: 3,
            STATISTIC: 4
        };
    };

    constructor(props) {
        super(props);

        this.state = {
            selected: LeftMenu.MENU_ELEM.SETTINGS
        };
    }

    render() {
        const {intl} = this.props;
        const settingsUrl = makeTemplateStr(routers.botSettings(), {id: this.props.id});
        const constructorUrl = makeTemplateStr(routers.botSetup(), {id: this.props.id});
        const statisticUrl = makeTemplateStr(routers.botStatistic(), {id: this.props.id});
        const broadcastingUrl = makeTemplateStr(routers.botBroadcasting(), {id: this.props.id});
        const votesUrl = makeTemplateStr(routers.botVotes(), {id: this.props.id});
        const items = [
            {
                label: intl.formatMessage({id: 'app.menu.settings'}),
                icon: 'pi pi-fw pi-cog',
                className: this.state.selected === LeftMenu.MENU_ELEM.SETTINGS ? "menu-item active" : 'menu-item',
                command: () => {
                    this.props.history.push(settingsUrl);
                    this.setState({selected: LeftMenu.MENU_ELEM.SETTINGS});
                }
            },
            {
                label: intl.formatMessage({id: 'app.menu.constructor'}),
                icon: 'pi pi-table',
                className: this.state.selected === LeftMenu.MENU_ELEM.CONSTRUCTOR ? "menu-item active" : 'menu-item',
                command: () => {
                    this.props.history.push(constructorUrl);
                    this.setState({selected: LeftMenu.MENU_ELEM.CONSTRUCTOR});
                }
            },
            {
                label: "Опросы",
                icon: 'pi pi-user',
                className: this.state.selected === LeftMenu.MENU_ELEM.VOTES ? "menu-item active" : 'menu-item',
                command: () => {
                    this.props.history.push(votesUrl);
                    this.setState({selected: LeftMenu.MENU_ELEM.VOTES});
                }
            },
            {
                label: intl.formatMessage({id: 'app.menu.broadcasting'}),
                icon: 'pi pi-envelope',
                className: this.state.selected === LeftMenu.MENU_ELEM.BROADCASTING ? "menu-item active" : 'menu-item',
                command: () => {
                    this.props.history.push(broadcastingUrl);
                    this.setState({selected: LeftMenu.MENU_ELEM.BROADCASTING});
                }
            },
            {
                label: intl.formatMessage({id: 'app.menu.statistic'}),
                icon: 'pi pi-chart-bar',
                className: this.state.selected === LeftMenu.MENU_ELEM.STATISTIC ? "menu-item active" : 'menu-item',
                command: () => {
                    this.props.history.push(statisticUrl);
                    this.setState({selected: LeftMenu.MENU_ELEM.STATISTIC});
                }
            },
        ];

        return (
            <Menu model={items} className="left-menu"/>
        );
    }
}

export default withRouter(connect()(injectIntl(LeftMenu)));
