import React, {Component} from 'react';
import {Menu} from 'primereact/menu';
import {connect} from "react-redux";
import {injectIntl} from "react-intl";
import * as routers from 'app/constants/routes';
import makeUrl from 'app/utils/makeUrl';

class LeftMenu extends Component {
    constructor(props) {
        super(props);
        const {intl} = this.props;
        const settingsUrl = makeUrl(routers.botSettings(), {id: this.props.id});
        const constructorUrl = makeUrl(routers.botSetup(), {id: this.props.id});
        const statisticUrl = makeUrl(routers.botStatistic(), {id: this.props.id});

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
            <Menu model={this.state.items} style={{minHeight: '70vh'}}/>
        );
    }
}

export default connect()(injectIntl(LeftMenu));
