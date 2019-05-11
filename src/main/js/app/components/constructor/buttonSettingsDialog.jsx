import {BaseDialog} from "app/components/baseDialog";
import {Button} from "primereact/button";
import React from "react";
import {Growl} from "primereact/growl";
import {Dialog} from "primereact/dialog";
import {injectIntl} from "react-intl";
import {InputText} from "primereact/inputtext";
import {Dropdown} from "primereact/dropdown";
import {InputTextarea} from "primereact/inputtextarea";
import * as BuilderService from "app/service/builder";
import BotConstructor from "app/components/pages/botConstructor";

class ButtonSettingsDialog extends BaseDialog {
    constructor(props) {
        super(props);

        this.state = {
            text: "text",
            value: "default",
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

    onShow(button, row, col, message) {
        super.onShow();
        this.setState({
            text: button.text,
            value: button.value,
            intent: button.intent,
            nextState: button.nextState,
            row: row,
            col: col,
            message: message
        });
    }

    addMessage() {
        BuilderService.newComponent(this.props.botSchemeId, (res) => {
            let messageObj = {};
            messageObj.buttonsGroup = null;
            messageObj.component = {
                groupId: null,
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
        console.log(this.state);
        const btn = {
            text: this.state.text,
            value: this.state.value,
            intent: this.state.intent,
            nextState: this.state.nextState
        };
        this.props.saveButton(btn, this.state.row, this.state.col);
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
            {label: "default", value: "default"},
            {label: "positive", value: "positive"},
            {label: "negative", value: "negative"},
        ];
        return (
            <div>
                <Growl ref={(el) => this.growl = el}/>
                <Dialog closable={false} footer={footer} visible={this.state.visible} className="dialog"
                        appendTo={document.body}
                        modal={true} onHide={this.onHide}>
                    <div className={"form"}>
                        <div className={"form_detail-element"}>
                            <InputText value={this.state.text}
                                       onChange={(e) => {
                                           this.setState({
                                               text: e.target.value,
                                               value: e.target.value
                                           });
                                       }}/>
                        </div>
                        <div className={"form_detail-element"}>
                            <Dropdown style={{width: '170px'}} value={this.state.intent} options={intents}
                                      onChange={(e) => this.setState({intent: e.value})}
                                      editable={true} placeholder="Select intent"/>
                        </div>
                        <div className={"form_detail-element"}>
                            <Dropdown style={{width: '170px'}}
                                      value={this.state.message ? this.state.message.component.nextState : this.state.nextState}
                                      options={this.props.nextComponentList}
                                      onChange={(e) => {
                                          if (this.state.message) {
                                              this.state.message.component.nextState = e.target.value;
                                              this.setState({message: this.state.message});
                                          } else {
                                              this.setState({nextState: e.value})
                                          }
                                      }
                                      }
                                      editable={true} placeholder="Select next component"/>
                        </div>
                        <div className={"form_detail-element"}>
                            <Button label={"Remove button"}
                                    onClick={this.removeButton}/>
                        </div>
                        <div className={"form_detail-element"}>
                            {!this.state.message && <Button label={"Add message"}
                                                            onClick={this.addMessage}/>}
                            {this.state.message && <Button label={"Remove message"}
                                                           onClick={() => {
                                                               this.setState({
                                                                   nextState: this.state.message.component.nextState,
                                                                   message: null
                                                               });
                                                               this.props.removeComponent(this.state.message, 0);
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
