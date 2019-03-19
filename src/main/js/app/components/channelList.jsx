import React from "react";
import {Card} from "primereact/card";
import {Button} from "primereact/button";
import * as ChatChannelService from "app/service/chatChannel"
import * as AxiosMessages from "app/utils/axiosMessages";
import {Growl} from "primereact/growl";
import {connect} from "react-redux";
import {injectIntl} from "react-intl";

class ChannelList extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            availableChannels: {},
            userChannels: {}
        };
        this.refreshChannels = this.refreshChannels.bind(this);
    }

    componentDidMount() {
        const self = this;

        ChatChannelService.getChannels(this.props.botSchemeId, (res) => {
            let channels = {};
            if (res.data.success) {
                res.data.chatChannels.forEach((channel) => {
                    channels[channel.id.chatId] = channel;
                });
            } else {
                AxiosMessages.serverErrorResponse(self, res.data.error);
            }
            self.setState({userChannels: channels});
        }, null, this);
    }

    refreshChannels() {
        const self = this;
        ChatChannelService.getTamChatsWhereAdmin(this.props.botSchemeId, (res) => {
            let channels = {};
            if (res.data.success) {
                res.data.chatChannels.forEach((channel) => {
                    if (!(channel.chat_id in self.state.userChannels)) {
                        channels[channel.chat_id] = channel;
                    }
                });
            } else {
                AxiosMessages.serverErrorResponse(self, res.data.error);
            }
            self.setState({availableChannels: channels});
        }, null, this);
    }

    getAvailableChannelsList() {
        const self = this;

        function append(chat_id) {
            ChatChannelService.storeChannel(self.props.botSchemeId, chat_id, (res) => {
                console.log(res)
            }, null, this);
        }

        let renderedAvailableChannels = [];
        for (let chat_id in this.state.availableChannels) {
            const footer = (
                <span>
                    <Button label={'Append'} icon="pi pi-angle-up" onClick={() => append(chat_id)}/>
                </span>
            );
            renderedAvailableChannels.push(<Card title={this.state.availableChannels[chat_id].title} footer={footer}
                                                 className={"p-col channel-list-element"}/>);
        }
        return renderedAvailableChannels;
    }

    getUserChannelsList() {
        const self = this;

        let renderedUserChannels = [];
        for (let chat_id in this.state.userChannels) {
            const footer = (
                <span>
                    <Button label={'remove'} className={'p-button-secondary'} icon="pi pi-times"/>
                </span>
            );
            renderedUserChannels.push(<Card title={this.state.userChannels[chat_id].title}
                                            className={"p-col channel-list-element"}/>);
        }
        return renderedUserChannels;
    }

    render() {
        const availableChannels = this.getAvailableChannelsList();
        const userChannels = this.getUserChannelsList();

        return (
            <div>
                <Growl ref={(el) => this.growl = el}/>
                <div className={"p-grid"}>
                    {userChannels}
                </div>
                <Button label={'Refresh'} icon="pi pi-refresh" onClick={this.refreshChannels}/>
                <div className={"p-grid"}>
                    {availableChannels}
                </div>
            </div>
        );
    }
}

export default connect()(injectIntl(ChannelList));