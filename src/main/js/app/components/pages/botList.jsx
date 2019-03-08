import React from 'react';
import {connect} from 'react-redux';
import {Card} from 'primereact/card';
import {Button} from 'primereact/button';
import * as routers from 'app/constants/routes';
import {injectIntl} from 'react-intl';
import {Redirect} from 'react-router-dom'
import axios from 'axios';
import * as ApiPoints from 'app/constants/apiPoints';
import {Growl} from "primereact/growl";
import * as AxiosMessages from 'app/utils/axiosMessages';
import makeUrl from 'app/utils/makeUrl'

class BotList extends React.Component {
    constructor(props) {
        super(props);
        this.state = {bots: []};
        this.onAddBot = this.onAddBot.bind(this);
        this.refreshBotList = this.refreshBotList.bind(this);
        this.createBotList = this.createBotList.bind(this);
    }

    refreshBotList() {
        axios.get(ApiPoints.BOT_LIST).then(res => {
            if (res.status === 200) {
                this.setState({bots: res.data});
            }
        }).catch(() => {
            AxiosMessages.serverNotResponse(this);
        });
    }

    componentDidMount() {
        this.refreshBotList();
    }

    onAddBot() {
        const defaultName = 'Tam tam bot';

        axios.post(ApiPoints.ADD_BOT,
            {name: defaultName}
        ).then(res => {
            if (Number.isInteger(res.data.id) && res.status === 200) {
                const url = makeUrl(routers.botDetail(), {id: res.data.id});
                this.props.history.push(url);
            } else {
                AxiosMessages.serverErrorResponse(this);
            }
        }).catch(error => {
            AxiosMessages.serverNotResponse(this);
        });
    }

    createBotList() {
        const {intl} = this.props;
        const self = this;
        if (!Array.isArray(this.state.bots)) {
            return null;
        }

        return this.state.bots.map((bot) => {
            function removeBot() {
                if (confirm(intl.formatMessage({id: 'app.dialog.checksure'}))) {
                    axios.delete(ApiPoints.DELETE_BOT, {
                        data: {
                            id: bot.id
                        }
                    }).then(() => {
                        self.refreshBotList();
                    });
                }
            }

            const header = (
                <img src={bot.img}/>
            );

            const footer = (
                <span className="p-grid p-justify-between">
                    <Button label={intl.formatMessage({id: 'app.bot.edit'})} icon="pi pi-pencil"
                            onClick={() => this.props.history.push(routers.botList() + String(bot.id) + "/")}
                            className={"p-col"}/>
                    <Button label={intl.formatMessage({id: 'app.bot.remove'})} icon="pi pi-times" onClick={removeBot}
                            className={"p-col p-button-secondary"}/>
                </span>
            );

            return (
                <Card title={bot.name}
                      className="ui-card-shadow bot_card p-col" footer={footer} header={header}>
                    <div>{bot.description}</div>
                </Card>
            );
        });
    }

    render() {
        if (!this.props.isLogin) {
            return (<Redirect to='/'/>);
        }

        const bots = this.createBotList();

        return (
            <div className={'p-grid'}>
                <Growl ref={(el) => this.growl = el}/>
                {bots}
                <Button className="p-button-rounded add-bot-button" icon='pi pi-plus' label='' onClick={this.onAddBot}/>
            </div>
        );
    }
}

const mapStateToProps = state => ({
    userId: state.userInfo.userId,
    isLogin: state.userInfo.userId != null
});

export default connect(mapStateToProps)(injectIntl(BotList));
