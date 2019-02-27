import React from 'react';
import {connect} from 'react-redux';
import {Card} from 'primereact/card';
import {Button} from 'primereact/button';
import AddBotDialog from 'app/components/addBotDialog';
import * as routers from 'app/constants/routes';
import {injectIntl} from 'react-intl';
import {Redirect} from 'react-router-dom'

class BotList extends React.Component {
    constructor(props) {
        super(props);
        this.addBotDialog = React.createRef();
        this.onAddBot = this.onAddBot.bind(this);

        //Hardcoded values
        //TODO: remove, when backend will be available
        this.state = {
            bots: [
                {
                    id: 1,
                    name: 'bot 1',
                    description: 'description for bot 1',
                    img: '/assets/images/tmp/usercard.png'
                },
                {
                    id: 2,
                    name: 'bot 2',
                    description: 'description for bot 2',
                    img: '/assets/images/tmp/usercard.png'
                },
            ]
        }
    }

    componentDidMount() {
        //TODO: get list bots to this.state.bots
    }

    onAddBot() {
        this.addBotDialog.current.getWrappedInstance().onShow();
    }

    createBotList() {
        const {intl} = this.props;
        return this.state.bots.map((bot) => {
            function removeBot() {
                if (confirm("Are you sure about this?")) {
                    //TODO: delete request
                }
            }

            const header = (
                <img src={bot.img}/>
            );

            const footer = (
                <span>
                    <Button label={intl.formatMessage({id: 'app.bot.edit'})} icon="pi pi-pencil"
                            onClick={() => this.props.history.push(routers.botList() + String(bot.id) + "/")}
                            style={{marginRight: '6px'}}/>
                    <Button label={intl.formatMessage({id: 'app.bot.remove'})} icon="pi pi-times" onClick={removeBot}
                            className="p-button-secondary"/>
                </span>
            );

            return (
                <Card title={bot.name}
                      style={{width: '280px', margin: '14px'}}
                      className="ui-card-shadow" footer={footer} header={header}>
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
            <div style={{display: 'flex', flexFlow: 'row wrap'}}>
                {bots}
                <Button className="p-button-rounded" icon='pi pi-plus' label='' onClick={this.onAddBot}
                        style={{position: 'absolute', right: '16px', bottom: '16px', width: '70px', height: '70px'}}/>
                <AddBotDialog ref={this.addBotDialog}/>
            </div>
        );
    }
}

const mapStateToProps = state => ({
    userId: state.userInfo.userId,
    isLogin: state.userInfo.userId != null
});

export default connect(mapStateToProps)(injectIntl(BotList));
