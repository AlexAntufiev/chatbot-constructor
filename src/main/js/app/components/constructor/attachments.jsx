import * as React from "react";
import {Button} from "primereact/button";
import BroadcastMessageState from "app/utils/broadcastMessageState";
import {ProgressBar} from "primereact/progressbar";
import {injectIntl} from "react-intl";
import * as BroadcastMessageService from "app/service/broadcastMessage";
import * as TamBotService from "app/service/tamBot";
import {Growl} from "primereact/growl";

class Attachments extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            attachments: [],
            ajaxUploadAttachProcess: false,
            attachmentChanged: false
        };

        this.createAttachElementsList = this.createAttachElementsList.bind(this);
        this.uploadFilesToTam = this.uploadFilesToTam.bind(this);
        this.saveAttachments = this.saveAttachments.bind(this);
        this.refreshAttachments = this.refreshAttachments.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (!prevState || prevState.attachmentChanged !== this.state.attachmentChanged) {
            if (this.props.attachmentChanged) {
                this.props.attachmentChanged(this.state.attachmentChanged)
            }
        }
    }

    uploadFilesToTam(files) {
        if (files.length === 0) {
            return;
        }
        this.setState({
            ajaxUploadAttachProcess: true,
            attachmentChanged: true
        });
        let uploaded = 0;
        for (let i = 0; i < files.length; ++i) {
            TamBotService.getAttachmentUploadLink(this.props.botSchemeId, "photo",
                (res) => {
                    const data = new FormData();
                    const xhr = new XMLHttpRequest();
                    data.append('data', files[i]);
                    xhr.open('POST', res.data.payload.url, true);
                    const self = this;
                    xhr.onreadystatechange = function () {
                        if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                            const uploadedObj = JSON.parse(xhr.responseText);
                            const firstKey = Object.keys(uploadedObj["photos"])[0];
                            let attachments = self.state.attachments.slice();
                            attachments.push({
                                title: files[i].name,
                                token: uploadedObj["photos"][firstKey].token,
                                type: "photo"
                            });
                            self.setState({attachments: attachments});
                        }
                        uploaded++;
                        if (uploaded === files.length) {
                            self.setState({ajaxUploadAttachProcess: false});
                            self.uploadButton.type = "";
                            self.uploadButton.type = "file";
                        }
                    };
                    xhr.send(data);
                }, () => {
                    uploaded++;
                    if (uploaded === files.length) {
                        self.setState({ajaxUploadAttachProcess: false});
                        self.uploadButton.type = "";
                        self.uploadButton.type = "file";
                    }
                }, this);
        }
    }

    saveAttachments(callbackSuccess = () => {}, callbackFalse = () => {}) {
        BroadcastMessageService.addAndRemoveAttachments(this.props.botSchemeId, this.props.chatChannelId, this.props.message.id,
            this.state.attachments, () => {
                callbackSuccess();
                this.setState({attachmentChanged: false});
                this.refreshAttachments();
            }, () => {
                callbackFalse();
                this.setState({attachmentChanged: false});
                this.refreshAttachments();
            }, this);
    }

    refreshAttachments() {
        BroadcastMessageService.getAttacmentsList(this.props.botSchemeId, this.props.chatChannelId, this.props.message.id, (res) => {
            let attachments = [];
            res.data.payload.forEach((attach) => {
                attachments.push({
                    title: attach.title,
                    id: attach.id,
                    token: attach.attachmentIdentifier,
                    type: "photo"
                });
            });
            this.setState({attachments: attachments});
        });
    }

    createAttachElementsList() {
        let renderedList = [];
        for (let i = 0; i < this.state.attachments.length; i++) {
            const attach = this.state.attachments[i];
            if (!attach.removed) {
                renderedList.push(<div className={"attach-element"}>
                    <div className={"table-cell attach-title"}>{attach.title}</div>
                    <div className={"table-cell"}><Button
                        disabled={this.props.message.state === BroadcastMessageState.SENT
                        || this.props.message.state === BroadcastMessageState.ERASED_BY_SCHEDULE}
                        icon={"pi pi-times"}
                        onClick={() => {
                            let attachments = this.state.attachments.slice();
                            attachments[i].removed = true;
                            this.setState({
                                attachments: attachments,
                                attachmentChanged: true
                            });
                        }}/>
                    </div>
                </div>)
            }
        }
        return renderedList;
    }

    render() {
        const {intl} = this.props;
        const attachments = this.createAttachElementsList();

        return (
            <div className="text-card_detail-element">
                <Growl ref={(el) => this.growl = el}/>
                <Button icon={"pi pi-paperclip"} className={'attach-button'} ref={(obj) => this.attachButton = obj}
                        label={intl.formatMessage({id: "app.dialog.attach"})}
                        onClick={() => this.uploadButton.click()}
                        disabled={this.state.ajaxUploadAttachProcess}/>
                {this.state.ajaxUploadAttachProcess &&
                <ProgressBar mode="indeterminate" className={'attach-progressbar'}/>}
                <input accept="image/*" className={"attach-input"} name={"data"}
                       ref={(obj) => this.uploadButton = obj} type={"file"}
                       multiple={true} onInput={(event) => this.uploadFilesToTam(event.target.files)}/>
                <div className={"attach-container"}>
                    {attachments}
                </div>
            </div>
        );
    }

}

export default injectIntl(Attachments, {withRef: true});
