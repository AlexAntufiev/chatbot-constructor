import React, {Component} from 'react';
import {Growl} from "primereact/growl";
import {Button} from "primereact/button";
import {Card} from "primereact/card";
import {Route, Switch, withRouter} from "react-router";
import {connect} from "react-redux";
import {injectIntl} from "react-intl";
import * as routes from 'app/constants/routes';
import makeUrl from 'app/utils/makeUrl';

class BotBroadcasting extends Component {
    constructor(props) {
        super(props);
        this.onChannelClick = this.onChannelClick.bind(this);
    }

    onChannelClick(channelId) {
        const url = makeUrl(routes.botBroadcastingDetail(), {
            id: this.props.match.params.id,
            channelId: channelId
        });

        this.props.history.push(url);
    }

    render() {
        const header = (
            <img/>
        );

        const footer = (
            <span className="p-grid p-justify-between">
                    <Button label="Edit" icon="pi pi-pencil"
                            className={"p-col"} onClick={() => this.onChannelClick(1)}/>
                    <Button label="Remove" icon="pi pi-times"
                            className={"p-col p-button-secondary"}/>
                </span>
        );
        return (<div className="p-grid bot-broadcasting_list">
            <Growl ref={(el) => this.growl = el}/>
            <Card title="channel 1"
                  className="ui-card-shadow card-container p-col" footer={footer} header={header}>
                <div>Description 1</div>
            </Card>
            <Card title="channel 2"
                  className="ui-card-shadow card-container p-col" footer={footer} header={header}>
                <div>Description 2</div>
            </Card>
        </div>);
    }
}

export default withRouter(connect()(injectIntl(BotBroadcasting)));
