import {Card} from "primereact/card";
import React from "react";
import {OverlayPanel} from "primereact/overlaypanel";
import customBotIcon from "images/customBotAvatar.jpg";

export default class ChatChannelCard extends React.Component {

    render() {
        let channelLinkName = '';
        if (this.props.chatChannel.link) {
            const tokens = this.props.chatChannel.link.split('/');
            channelLinkName = '@' + tokens[tokens.length - 1];
        }
        if (!this.props.chatChannel.icon) {
            this.props.chatChannel.icon = customBotIcon;
        }
        const header = (
            <div>
                <img src={this.props.chatChannel.icon} className="channel-list-element_image"/>
                {this.props.chatChannel.link &&
                <a href={this.props.chatChannel.link} target="_blank"
                   className="channel-list-element_link">{channelLinkName}</a>}
            </div>
        );

        return (
            <Card title={this.props.chatChannel.title} footer={this.props.footer}
                  header={header}
                  className={"p-col channel-list-element"}>
                <span className="pi pi-search full-info"
                      onClick={(e) => this.overlayPanel.toggle(e)}/>
                <OverlayPanel ref={(el) => this.overlayPanel = el} appendTo={document.body}
                              className={"channel-list-element_overlay-panel"}>
                    <div className="image-container">
                        <img src={this.props.chatChannel.icon} className="channel-list-element_overlay-panel_image"/>
                    </div>
                    <div>
                        <div className="channel-list-element_overlay-panel_title">{this.props.chatChannel.title}</div>
                        {this.props.chatChannel.link &&
                        <a href={this.props.chatChannel.link} target="_blank"
                           className="channel-list-element_overlay-panel_link">{channelLinkName}</a>}
                        <div>{this.props.chatChannel.description}</div>
                    </div>
                </OverlayPanel>
                {this.props.chatChannel.description}
            </Card>
        );
    }
}
