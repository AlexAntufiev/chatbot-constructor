import {BaseDialog} from "app/components/baseDialog";
import {Button} from "primereact/button";
import React from "react";
import {Growl} from "primereact/growl";
import {Dialog} from "primereact/dialog";
import {FormattedMessage, injectIntl} from "react-intl";
import {InputText} from "primereact/inputtext";
import {Dropdown} from "primereact/dropdown";
import {InputTextarea} from "primereact/inputtextarea";
import * as BuilderService from "app/service/builder";
import BotConstructor from "app/components/pages/botConstructor";
import * as AxiosMessages from 'app/utils/axiosMessages';

class ButtonSettingsDialog extends BaseDialog {
    constructor(props) {
        super(props);

        this.state = {
            text: "text",
            value: "text",
            intent: "default",
            nextState: null,
            message: null,
            row: null,
            col: null
        };
        this.addMessage = this.addMessage.bind(this);
        this.onSave = this.onSave.bind(this);
        this.removeButton = this.removeButton.bind(this);
    }

    onShow(button, row, col, message, groupId) {
        super.onShow();
        this.setState({
            text: button.text,
            value: button.value,
            intent: button.intent,
            nextState: button.nextState,
            row: row,
            col: col,
            message: message,
            groupId: groupId
        });
    }

    addMessage() {
        BuilderService.newComponent(this.props.botSchemeId, (res) => {
            let messageObj = {};
            messageObj.buttonsGroup = null;
            messageObj.component = {
                groupId: this.state.groupId,
                id: res.data.payload.componentId,
                nextState: this.state.nextState,
                schemeId: this.props.botSchemeId,
                text: "",
                type: BotConstructor.COMPONENT_SCHEME_TYPES.INFO
            };
            this.props.appendComponent(messageObj);
            this.setState({
                message: messageObj,
                nextState: res.data.payload.componentId
            });
        }, null, this);
    }


    removeButton() {
        this.props.removeButton(this.state.row, this.state.col);
        this.onHide();
    }

    onSave() {
        const {intl} = this.props;
        if (this.state.message && this.state.message.component.text === '') {
            AxiosMessages.customError(this, intl.formatMessage({id: 'app.constructor.error.fill.text'}));
            return;
        }
        if (this.state.text.trim() === '') {
            AxiosMessages.customError(this, intl.formatMessage({id: 'app.constructor.error.empty.title'}));
            return;
        }
        const btn = {
            text: this.state.text,
            value: this.state.value,
            intent: this.state.intent,
            nextState: this.state.nextState
        };
        this.props.saveButton(btn, this.state.row, this.state.col);
        this.props.onChange(this.state.message);
        this.onHide();
    }

    render() {
        const {intl} = this.props;
        const footer = (
            <div>
                <Button label={intl.formatMessage({id: 'app.dialog.save'})} icon="pi pi-check" onClick={this.onSave}/>
                <Button label={intl.formatMessage({id: 'app.dialog.close'})} icon="pi pi-times" onClick={this.onHide}
                        className="p-button-secondary"/>
            </div>
        );

        const intents = [
            {label: intl.formatMessage({id: 'app.constructor.intent.default'}), value: "default"},
            {label: intl.formatMessage({id: 'app.constructor.intent.positive'}), value: "positive"},
            {label: intl.formatMessage({id: 'app.constructor.intent.negative'}), value: "negative"},
        ];
        return (
            <div>
                <Growl ref={(el) => this.growl = el} baseZIndex={2000}/>
                <Dialog closable={false} footer={footer} visible={this.state.visible} className="dialog"
                        appendTo={document.body}
                        modal={true} onHide={this.onHide}>
                    <div className={"form"}>
                        <div className={"form_detail-element"}>
                            <div className={"element-label"}><FormattedMessage id={'app.dialog.name'}/></div>
                            <InputText className={'full-width'} value={this.state.text}
                                       onChange={(e) => {
                                           this.setState({
                                               text: e.target.value,
                                               value: e.target.value
                                           });
                                       }}
                                       placeholder={intl.formatMessage({id: 'app.dialog.name'})}/>
                        </div>
                        <div className={"form_detail-element"}>
                            <div className={"element-label"}><FormattedMessage id={'app.constructor.intent'}/></div>
                            <Dropdown className={'full-width'} value={this.state.intent} options={intents}
                                      onChange={(e) => this.setState({intent: e.value})}/>
                        </div>
                        <div className={"form_detail-element"}>
                            <div className={"element-label"}><FormattedMessage id={'app.constructor.next.component'}/>
                            </div>
                            <Dropdown className={'full-width'}
                                      value={this.state.message ? this.state.message.component.nextState : this.state.nextState}
                                      options={this.props.nextComponentList}
                                      onChange={(e) => {
                                          if (this.state.message) {
                                              this.state.message.component.nextState = e.value;
                                              this.setState({message: this.state.message});
                                          } else {
                                              this.setState({nextState: e.value})
                                          }
                                      }
                                      }/>
                        </div>
                        <div className={"form_detail-element"}>
                            <Button label={intl.formatMessage({id: 'app.dialog.remove.button'})}
                                    onClick={this.removeButton}/>
                        </div>
                        <div className={"form_detail-element"}>
                            {!this.state.message &&
                            <Button label={intl.formatMessage({id: 'app.dialog.add.message'})}
                                    onClick={this.addMessage}/>}
                            {this.state.message && <Button label={intl.formatMessage({id: 'app.dialog.remove.message'})}
                                                           onClick={() => {
                                                               this.props.removeComponent(this.state.message);
                                                               this.setState({
                                                                   nextState: this.state.message.component.nextState,
                                                                   message: null
                                                               });
                                                           }}/>}
                        </div>
                        <div className={"form_detail-element"}>
                            {this.state.message &&
                            <InputTextarea rows={5} cols={30} value={this.state.message.component.text}
                                           onChange={(e) => {
                                               this.state.message.component.text = e.target.value;
                                               this.setState({message: this.state.message});
                                           }}/>}
                        </div>
                    </div>
                </Dialog>
            </div>
        );
    }
}

export default injectIntl(ButtonSettingsDialog, {withRef: true});
