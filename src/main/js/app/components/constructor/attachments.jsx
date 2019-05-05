import * as React from "react";
import {Button} from "primereact/button";
import BroadcastMessageState from "app/utils/broadcastMessageState";
import {ProgressBar} from "primereact/progressbar";
import {injectIntl} from "react-intl";
import * as BroadcastMessageService from "app/service/broadcastMessage";
import * as TamBotService from "app/service/tamBot";
import {Growl} from "primereact/growl";
import * as AxiosMessages from 'app/utils/axiosMessages';

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
        this.getCountByTypes = this.getCountByTypes.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (!prevState || prevState.attachmentChanged !== this.state.attachmentChanged) {
            if (this.props.onAttachmentChanged) {
                this.props.onAttachmentChanged(this.state.attachmentChanged)
            }
        }

        if (!prevState || prevState.ajaxUploadAttachProcess !== this.state.ajaxUploadAttachProcess) {
            if (this.props.onUploadProcess) {
                this.props.onUploadProcess(this.state.ajaxUploadAttachProcess)
            }
        }
    }

    static typeById(id) {
        switch (id) {
            case 0:
                return "photo";
            case 1:
                return "video";
            case 2:
                return "audio";
            default:
                return "file";
        }
    }

    static getTypeByStr(str) {
        if (str.startsWith("image/")) return "photo";
        if (str.startsWith("video/")) return "video";
        if (str.startsWith("audio/m4a")) return "audio";
        return "file";
    }

    static getTokenFromResp(res, type) {
        switch (type) {
            case "photo":
                const firstKey = Object.keys(res["photos"])[0];
                return res["photos"][firstKey].token;
            case "audio":
            case "video":
                return res.id;
            case "file":
                return res.fileId;
            default:
                return null;
        }
    }

    getCountByTypes() {
        let counts = {
            photo: 0,
            video: 0,
            audio: 0,
            file: 0
        };

        this.state.attachments.forEach((attachment) => {
            if (!attachment.removed) {
                counts[attachment.type]++;
            }
        });
        return counts;
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
        let counts = this.getCountByTypes();
        let reqCounts = files.length;
        for (let i = 0; i < files.length; ++i) {
            if (files[i].size > 2147483648) { // more than 2Gb
                continue;
            }
            const attachType = Attachments.getTypeByStr(files[i].type);
            const {intl} = this.props;
            //check attachments count
            counts[attachType]++;
            if (counts.file > 0 && (counts.photo > 0 || counts.video > 0 || counts.audio > 0)) {
                AxiosMessages.customError(this, intl.formatMessage({id: "errors.broadcast.message.has.too.much.attachments"}));
                counts[attachType]--;
                reqCounts--;
                continue;
            }
            if (counts.audio > 0 && (counts.photo > 0 || counts.video > 0 || counts.file > 0)) {
                AxiosMessages.customError(this, intl.formatMessage({id: "errors.broadcast.message.has.too.much.attachments"}));
                counts[attachType]--;
                reqCounts--;
                continue;
            }
            if (counts.photo > 10 || counts.video > 10 || (counts.video + counts.photo > 10)) {
                AxiosMessages.customError(this, intl.formatMessage({id: "errors.broadcast.message.has.too.much.attachments"}));
                counts[attachType]--;
                reqCounts--;
                continue;
            }
            TamBotService.getAttachmentUploadLink(this.props.botSchemeId, attachType,
                (res) => {
                    const data = new FormData();
                    const xhr = new XMLHttpRequest();
                    data.append('data', files[i]);
                    xhr.open('POST', res.data.payload.url, true);
                    const self = this;
                    xhr.onreadystatechange = function () {
                        if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                            const uploadedObj = JSON.parse(xhr.responseText);
                            let attachments = self.state.attachments.slice();
                            attachments.push({
                                title: files[i].name,
                                token: Attachments.getTokenFromResp(uploadedObj, attachType),
                                type: attachType
                            });
                            self.setState({attachments: attachments});
                        }
                        uploaded++;
                        if (uploaded === reqCounts) {
                            self.setState({ajaxUploadAttachProcess: false});
                            self.uploadButton.type = "";
                            self.uploadButton.type = "file";
                        }
                    };
                    xhr.send(data);
                }, () => {
                    uploaded++;
                    if (uploaded === reqCounts) {
                        this.setState({ajaxUploadAttachProcess: false});
                        this.uploadButton.type = "";
                        this.uploadButton.type = "file";
                    }
                }, this);
        }
        if (uploaded === reqCounts) {
            this.setState({ajaxUploadAttachProcess: false});
            this.uploadButton.type = "";
            this.uploadButton.type = "file";
        }
    }

    saveAttachments(callbackSuccess = () => {}, callbackFalse = () => {}) {
        BroadcastMessageService.removeAttachments(this.props.botSchemeId, this.props.chatChannelId, this.props.message.id,
            this.state.attachments, () => {
                BroadcastMessageService.addAttachments(this.props.botSchemeId, this.props.chatChannelId, this.props.message.id,
                    this.state.attachments, () => {
                        callbackSuccess();
                        this.setState({attachmentChanged: false});
                        this.refreshAttachments();
                    }, () => {
                        callbackFalse();
                        this.setState({attachmentChanged: false});
                        this.refreshAttachments();
                    }, this);
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
                    type: Attachments.typeById(attach.type)
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
                        disabled={this.state.ajaxUploadAttachProcess
                        || this.props.message.state === BroadcastMessageState.SENT
                        || this.props.message.state === BroadcastMessageState.ERASED_BY_SCHEDULE}/>
                {this.state.ajaxUploadAttachProcess &&
                <ProgressBar mode="indeterminate" className={'attach-progressbar'}/>}
                <input className={"attach-input"} name={"data"}
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
