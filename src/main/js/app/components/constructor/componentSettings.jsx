import React, {Component} from "react";
import {InputText} from "primereact/inputtext";
import {InputTextarea} from "primereact/inputtextarea";
import {Button} from "primereact/button";
import {withRouter} from "react-router";
import {injectIntl} from "react-intl";
import BotConstructor from "app/components/pages/botConstructor";
import ButtonSettingsDialog from "app/components/constructor/buttonSettingsDialog";

class ComponentSettings extends Component {
    constructor(props) {
        super(props);

        this.state = {
            title: '',
            text: '',
            nextState: 0,
            buttonsGroup: null,
            editedButton: {},

        };

        this.createNextStatesList = this.createNextStatesList.bind(this);
        this.createButtonsList = this.createButtonsList.bind(this);
        this.renderButton = this.renderButton.bind(this);
        this.createButton = this.createButton.bind(this);
        this.findButtonMessage = this.findButtonMessage.bind(this);
        this.saveButton = this.saveButton.bind(this);
        this.removeButton = this.removeButton.bind(this);
        this.checkEmptyRows = this.checkEmptyRows.bind(this);
        this.findMessage = this.findMessage.bind(this);
    }

    createNextStatesList() {
        let nextComponents = [];
        for (let groupId in this.props.components) {
            const group = this.props.components[groupId];
            for (let i = 0; i < group.length; i++) {
                const componentObj = group[i];
                //continue, if text message
                if (componentObj.component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INFO && !componentObj.buttonsGroup) {
                    continue;
                }
                //continue, if not info
                if (componentObj.component.type !== BotConstructor.COMPONENT_SCHEME_TYPES.INFO) {
                    continue;
                }
                nextComponents.push({
                    label: componentObj.component.title,
                    value: componentObj.component.id
                });
            }
        }
        return nextComponents;
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if ((prevProps.component && this.props.component && (prevProps.component.component.id !== this.props.component.component.id)) ||
            (!prevProps.component && this.props.component)) {
            this.setState({
                title: this.props.component.component.title,
                text: this.props.component.component.text,
                nextState: this.props.component.component.nextState,
                buttonsGroup: this.props.component.buttonsGroup
            });
            this.findButtonMessage();
        }

        if (!prevState ||
            (prevState &&
                (prevState.title !== this.state.title ||
                    prevState.text !== this.state.text ||
                    prevState.nextState !== this.state.nextState))) {
            let componentObj = Object.assign({}, this.props.component);
            componentObj.component.title = this.state.title;
            componentObj.component.text = this.state.text;
            componentObj.component.nextState = this.state.nextState;

            this.props.onChange(componentObj, this.props.groupId);
        }
    }

    saveButton(btn, row, col) {
        this.state.buttonsGroup.buttons[row][col] = btn;
        this.setState({buttonsGroup: this.state.buttonsGroup});
    }

    removeButton(row, col) {
        this.state.buttonsGroup.buttons[row].splice(col, 1);
        this.setState({buttonsGroup: this.state.buttonsGroup});
    }

    createButton(row) {
        let buttons = this.state.buttonsGroup.buttons.slice();
        const buttonObj = {
            intent: null,
            nextState: null,
            text: "button",
            value: "button"
        };

        if (row === -1) {
            buttons.push([buttonObj]);
        } else {
            buttons[row].push(buttonObj);
        }
        this.state.buttonsGroup.buttons = buttons;
        this.setState({buttonsGroup: this.state.buttonsGroup});
    }

    renderButton(elem, row, col) {
        return (
            <Button label={elem.text} className={"button-elem"} onClick={() => {
                this.settingsButtonDialog.getWrappedInstance().onShow(elem, row, col, this.findMessage(elem.nextState))
            }}/>
        );
    }

    createButtonsList() {
        if (!this.state.buttonsGroup) {
            return null;
        }
        if (this.state.buttonsGroup.buttons.length === 0) {
            return (
                <div>
                    <Button icon='pi pi-plus' className={"button-elem"} onClick={() => this.createButton(-1)}/>
                </div>
            );
        }
        let renderedMatrix = [];
        for (let i = 0; i < this.state.buttonsGroup.buttons.length; i++) {
            let renderedRow = [];
            for (let j = 0; j < this.state.buttonsGroup.buttons[i].length; j++) {
                renderedRow.push(this.renderButton(this.state.buttonsGroup.buttons[i][j], i, j));
            }
            if (renderedRow.length < 5) {
                renderedRow.push(<Button icon='pi pi-plus' onClick={() => this.createButton(i)}/>);
            }
            renderedMatrix.push(<div>{renderedRow}</div>);
        }
        if (renderedMatrix.length < 5) {
            renderedMatrix.push(<div><Button icon='pi pi-plus' className={"button-elem"}
                                             onClick={() => this.createButton(-1)}/></div>);
        }
        return renderedMatrix;
    }

    findButtonMessage() {
        const componentId = Number(this.props.match.params.componentId);
        const ind = this.props.findComponentInd(0, componentId);
        const messageId = this.props.components[0][ind];
        const messageInd = this.props.findComponentInd(0, messageId);
        if (messageInd === -1) {
            this.setState({editedButton: null});
        } else {
            this.setState({editedButton: this.props.components[0][messageInd]});
        }

    }

    checkEmptyRows() {
        if (!this.state.buttonsGroup)
        {
            return;
        }
        let i = 0;
        let changed = false;
        while (i < this.state.buttonsGroup.buttons.length) {
            if (this.state.buttonsGroup.buttons[i].length === 0) {
                this.state.buttonsGroup.buttons.splice(i, 1);
                changed = true;
            } else {
                i++;
            }
        }
        if (changed) {
            this.setState({buttonsGroup: this.state.buttonsGroup});
        }
    }

    findMessage(idBtn) {
        if (!idBtn) {
            return null;
        }
        const ind = this.props.findComponentInd(0, idBtn);
        if (ind !== -1) {
            const component = this.props.components[0][ind];
            if (component.component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INFO && !component.buttonsGroup) {
                return component;
            }
        }
        return null;
    }

    render() {
        if (!this.props.component) {
            return (<div/>);
        }
        this.checkEmptyRows();
        const nextComponentList = this.createNextStatesList();
        const buttonsList = this.createButtonsList();

        return (
            <div className={"text-card"}>
                <div className={"text-card_detail-element"}>
                    <InputText value={this.state.title} onChange={(e) => this.setState({title: e.target.value})}/>
                </div>
                <div className={"text-card_detail-element"}>
                    <InputTextarea rows={5} cols={60} value={this.state.text}
                                   onChange={(e) => this.setState({text: e.target.value})}/>
                </div>
                {/*<div className={"text-card_detail-element"}>
                    <Dropdown value={this.state.nextState} options={nextComponentList}
                              onChange={(e) => this.setState({nextState: e.value})}
                              editable={true} placeholder="Select next component"/>
                </div>*/}
                <div className="text-card_button-panel">
                    <Button label={"Remove"}
                            onClick={() => this.props.onRemove(this.props.component, this.props.groupId)}/>
                </div>
                <div className={"button-panel"}>
                    {buttonsList}
                </div>
                <ButtonSettingsDialog nextComponentList={nextComponentList}
                                      botSchemeId={Number(this.props.match.params.id)}
                                      removeComponent={this.props.onRemove}
                                      ref={(obj) => this.settingsButtonDialog = obj}
                                      id={this.props.component.component.id}
                                      appendComponent={this.props.appendComponent}
                                      button={this.state.editedButton}
                                      saveButton={this.saveButton}
                                      removeButton={this.removeButton}/>
            </div>
        );
    }
}

export default withRouter(injectIntl(ComponentSettings, {withRef: true}));
