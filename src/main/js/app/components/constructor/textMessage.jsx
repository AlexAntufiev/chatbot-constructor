import {InputText} from "primereact/inputtext";
import {Calendar} from "primereact/calendar";
import {InputTextarea} from "primereact/inputtextarea";
import {Button} from "primereact/button";
import React from "react";
import * as BroadcastMessageService from "app/service/broadcastMessage";
import * as TamBotService from "app/service/tamBot";
import {Growl} from "primereact/growl";
import * as AxiosMessages from 'app/utils/axiosMessages';
import * as Routes from 'app/constants/routes';
import makeUrl from "app/utils/makeUrl";
import BroadcastMessageState from 'app/utils/broadcastMessageState';
import shallowequal from "shallowequal";
import getCalendar from "app/i18n/calendarLocale";
import {withRouter} from "react-router";
import {connect} from "react-redux";
import {injectIntl} from "react-intl";
import {ProgressBar} from 'primereact/progressbar';

class TextMessage extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            message: {
                title: "",
                text: "",
                firingTime: null,
                erasingTime: null
            },
            initialMessage: {
                title: "",
                text: "",
                firingTime: null,
                erasingTime: null
            },
            attachments: [],
            attachmentsWasChanged: false,
            ajaxUploadAttachProcess: false,
            ajaxUpdateProcess: false,
            ajaxRemoveProcess: false
        };
        this.onUpdateMessage = this.onUpdateMessage.bind(this);
        this.onRemoveMessage = this.onRemoveMessage.bind(this);
        this.getUpdatedFields = this.getUpdatedFields.bind(this);
        this.uploadFilesToTam = this.uploadFilesToTam.bind(this);
        this.createAttachElementsList = this.createAttachElementsList.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        const message = this.props.message;
        if (message && !shallowequal(prevProps.message, message)) {
            this.uploadButton.value = "";
            BroadcastMessageService.getAttacmentsList(this.props.botSchemeId, this.props.chatChannelId, this.props.message.id, (res) => {
                let attachments = [];
                res.data.payload.forEach((attach) => {
                    attachments.push({
                        title: attach.title,
                        id: attach.id,
                        token: attach.attachmentIdentifier
                    });
                });
                this.setState({attachments: attachments});
            });
            const firingTime = new Date(message.firingTime);
            const erasingTime = new Date(message.erasingTime);
            const newMessage = {
                title: message.title,
                firingTime: message.firingTime ? firingTime.getTime() : null,
                erasingTime: message.erasingTime ? erasingTime.getTime() : null,
                text: message.text ? message.text : "",
            };
            this.setState({
                message: Object.assign({}, newMessage),
                initialMessage: Object.assign({}, newMessage),
                ajaxUpdateProcess: false,
                ajaxRemoveProcess: false,
                attachmentsWasChanged: false,
                ajaxUploadAttachProcess: false
            });
        }
    }

    getUpdatedFields() {
        let message = Object.assign({}, this.state.message);
        for (let key in message) {
            if (message[key] === this.state.initialMessage[key]) {
                delete message[key];
            }
        }
        return message;
    }

    createAttachElementsList() {
        let renderedList = [];
        for (let i = 0; i < this.state.attachments.length; i++) {
            const attach = this.state.attachments[i];
            if (!attach.removed) {
                renderedList.push(<div className={"attach-element"}>
                    <div className={"table-cell attach-title"}>{attach.title}</div>
                    <div className={"table-cell"}><Button icon={"pi pi-times"} onClick={() => {
                        const attachments = [...this.state.attachments];
                        attachments[i].removed = true;
                        this.setState({
                            attachments: attachments,
                            attachmentsWasChanged: true
                        });
                    }}/></div>
                </div>)
            }
        }
        return renderedList;
    }

    uploadFilesToTam(files) {
        if (files.length === 0) {
            return;
        }
        this.setState({
            ajaxUploadAttachProcess: true,
            attachmentsWasChanged: true
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
                            self.setState({
                                attachments: [...self.state.attachments, {
                                    title: files[i].name,
                                    img: URL.createObjectURL(files[i]),
                                    token: uploadedObj["photos"][firstKey].token
                                }]
                            });
                        }
                        uploaded++;
                        if (uploaded === files.length) {
                            self.setState({ajaxUploadAttachProcess: false});
                        }
                    };
                    xhr.send(data);
                }, () => {
                    uploaded++;
                    if (uploaded === files.length) {
                        self.setState({ajaxUploadAttachProcess: false});
                    }
                }, this);
        }
    }

    onUpdateMessage() {
        if (this.state.message.title.trim() === '') {
            AxiosMessages.serverErrorResponse(this, 'errors.broadcast.message.title.is.empty');
            return;
        }

        if (this.props.message.state === BroadcastMessageState.CREATED && this.state.message.text.trim() && !this.state.message.firingTime) {
            AxiosMessages.serverErrorResponse(this, 'error.broadcast.message.need.fill.text.and.posttime');
            return;
        }

        this.setState({ajaxUpdateProcess: true});
        let message = this.getUpdatedFields();
        //time fields send always
        if (this.state.message.firingTime) {
            message.firingTime = new Date(this.state.message.firingTime).toUTCString();
        }
        if (this.state.message.erasingTime) {
            message.erasingTime = new Date(this.state.message.erasingTime).toUTCString();
        }
        BroadcastMessageService.updateBroadcastMessage(this.props.botSchemeId, this.props.chatChannelId,
            this.props.message.id,
            message,
            () => {
                this.state.attachments.forEach((attach, i) => {
                    if (!attach.id && !attach.removed) {
                        BroadcastMessageService.addAttachment(this.props.botSchemeId, this.props.chatChannelId,
                            this.props.message.id, {
                                title: attach.title,
                                token: attach.token,
                                type: "photo"
                            }, (res) => {
                                this.state.attachments[i].id = res.data.payload.id;
                            }, null, this);
                    } else if (attach.removed && attach.id) {
                        BroadcastMessageService.removeAttachment(this.props.botSchemeId, this.props.chatChannelId,
                            this.props.message.id, attach.id, (res) => {
                                this.state.attachments.splice(i, 1);
                            }, null, this);
                    }

                });
                this.props.refreshMessageList();
                AxiosMessages.successOperation(this, 'app.broadcastmessage.saved');
                this.setState({
                    ajaxUpdateProcess: false,
                    initialMessage: Object.assign({}, this.state.message),
                    attachmentsWasChanged: false
                });
            },
            () => this.setState({ajaxUpdateProcess: false}),
            this);
    }

    onRemoveMessage() {
        const {intl} = this.props;
        if (confirm(intl.formatMessage({id: 'app.dialog.checksure'}))) {
            this.setState({ajaxRemoveProcess: true});
            BroadcastMessageService.removeBroadcastMessage(this.props.botSchemeId, this.props.chatChannelId, this.props.message.id,
                () => {
                    const url = makeUrl(Routes.botBroadcastingDetail(), {
                        id: this.props.botId,
                        botSchemeId: this.props.botSchemeId,
                        chatChannelId: this.props.chatChannelId
                    });
                    this.props.refreshMessageList();
                    this.setState({ajaxRemoveProcess: false});
                    this.props.history.push(url);
                }, () => this.setState({ajaxRemoveProcess: false}),
                this
            );
        }
    }

    render() {
        if (!this.props.message) {
            return (<div/>);
        }
        const {intl} = this.props;
        const changeMessageField = (field, value) => {
            let message = Object.assign({}, this.state.message);
            message[field] = value;
            this.setState({message: message});
        };
        const attachments = this.createAttachElementsList();
        let disableSave = this.state.ajaxUpdateProcess || Object.keys(this.getUpdatedFields()).length === 0;
        if (this.state.attachmentsWasChanged) {
            disableSave = false;
        }

        return (
            <div className='text-card'>
                <Growl ref={(el) => this.growl = el}/>
                <div className="text-card_detail-element">
                    <InputText placeholder={intl.formatMessage({id: 'app.dialog.name'})}
                               value={this.state.message.title}
                               onChange={(e) => changeMessageField('title', e.target.value)}/>
                </div>
                <div className="text-card_detail-element">
                    <Calendar showTime={true} showSeconds={false}
                              showButtonBar={true}
                              placeholder={intl.formatMessage({id: 'app.broadcastmessage.postingtime'})}
                              disabled={this.props.message.state === BroadcastMessageState.SENT}
                              onChange={(e) => {
                                  const newDate = e.value;
                                  if (newDate) {
                                      newDate.setSeconds(0);
                                  }
                                  changeMessageField('firingTime', newDate ? newDate.getTime() : null);
                              }}
                              dateFormat={"D, d M yy"}
                              hourFormat={this.props.locale === 'ru' ? "24" : "12"}
                              value={this.state.message.firingTime ? new Date(this.state.message.firingTime) : null}
                              locale={getCalendar(this.props.locale)}/>
                </div>
                <div className="text-card_detail-element">
                    <Calendar showTime={true} showSeconds={false}
                              showButtonBar={true}
                              placeholder={intl.formatMessage({id: 'app.broadcastmessage.erasingtime'})}
                              onChange={(e) => {
                                  const newDate = e.value;
                                  if (newDate) {
                                      newDate.setSeconds(0);
                                  }
                                  changeMessageField('erasingTime', newDate ? newDate.getTime() : null);
                              }}
                              dateFormat={"D, d M yy"}
                              hourFormat={this.props.locale === 'ru' ? "24" : "12"}
                              value={this.state.message.erasingTime ? new Date(this.state.message.erasingTime) : null}
                              locale={getCalendar(this.props.locale)}/>
                </div>
                <div className="text-card_detail-element">
                    <InputTextarea placeholder={intl.formatMessage({id: 'app.broadcastmessage.text'})}
                                   value={this.state.message.text} rows={5} cols={60}
                                   disabled={this.props.message.state === BroadcastMessageState.SENT}
                                   onChange={(e) => changeMessageField('text', e.target.value)}
                                   autoResize={true}
                                   maxLength={3000}/>
                </div>
                <div className="text-card_detail-element">
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
                <div className="text-card_button-panel">
                    <Button
                        label={intl.formatMessage({id: 'app.dialog.save'})}
                        onClick={this.onUpdateMessage}
                        disabled={disableSave}/>
                    <Button label={intl.formatMessage({id: 'app.bot.remove'})} onClick={this.onRemoveMessage}
                            disabled={this.state.ajaxRemoveProcess}/>
                </div>
            </div>
        );
    }
}

const mapStateToProps = state => ({
    locale: state.locale.locale
});

export default withRouter(connect(mapStateToProps)(injectIntl(TextMessage)));
