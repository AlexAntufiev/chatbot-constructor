import React, {Component} from "react";
import {InputText} from "primereact/inputtext";
import {InputTextarea} from "primereact/inputtextarea";
import {Button} from "primereact/button";
import {withRouter} from "react-router";
import {injectIntl} from "react-intl";
import BotConstructor from "app/components/pages/botConstructor";
import ButtonSettingsDialog from "app/components/constructor/buttonSettingsDialog";
import {Growl} from "primereact/growl";
import {Dropdown} from "primereact/dropdown";

class ComponentSettings extends Component {
    constructor(props) {
        super(props);

        this.state = {
            title: '',
            text: '',
            nextState: 0,
            buttonsGroup: null,
        };

        this.createNextStatesList = this.createNextStatesList.bind(this);
        this.createButtonsList = this.createButtonsList.bind(this);
        this.renderButton = this.renderButton.bind(this);
        this.createButton = this.createButton.bind(this);
        this.saveButton = this.saveButton.bind(this);
        this.removeButton = this.removeButton.bind(this);
        this.checkEmptyRows = this.checkEmptyRows.bind(this);
        this.findMessage = this.findMessage.bind(this);
    }

    createNextStatesList() {
        let nextComponents = [];
        const {intl} = this.props;
        for (let groupId in this.props.components) {
            const group = this.props.components[groupId];
            for (let i = 0; i < group.length; i++) {
                const componentObj = group[i];
                if (componentObj.component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INFO) {
                    const firstInVote = !componentObj.buttonsGroup
                        && this.props.groups[componentObj.component.groupId].type === BotConstructor.GROUP_TYPE.VOTE && i === 0;

                    if (componentObj.buttonsGroup || firstInVote)
                        nextComponents.push({
                            label: firstInVote ?
                                intl.formatMessage({id: 'app.constructor.component.vote'}) + " " + this.props.groups[componentObj.component.groupId].title :
                                this.props.groups[componentObj.component.groupId].title + ' - ' + componentObj.component.title,
                            value: componentObj.component.id
                        });
                }
            }
        }
        return nextComponents;
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        const groupId = Number(this.props.match.params.groupId);
        if ((prevProps.component && this.props.component && (prevProps.component.component.id !== this.props.component.component.id)) ||
            (!prevProps.component && this.props.component)) {
            let nextState = this.props.component.component.nextState;

            if (this.props.groups[groupId].type === BotConstructor.GROUP_TYPE.VOTE) {
                const ind = this.props.findComponentInd(groupId, this.props.component.component.nextState);
                nextState = this.props.components[groupId][ind].component.nextState;
            }
            this.setState({
                title: this.props.component.component.title,
                text: this.props.component.component.text,
                nextState: nextState,
                buttonsGroup: this.props.component.buttonsGroup
            });
        }

        if (!prevState ||
            (prevState &&
                (prevState.title !== this.state.title ||
                    prevState.text !== this.state.text ||
                    prevState.nextState !== this.state.nextState))) {


            let textComponentObj;
            textComponentObj = this.props.component;
            textComponentObj.component.title = this.state.title;
            textComponentObj.component.text = this.state.text;

            if (this.props.groups[groupId].type === BotConstructor.GROUP_TYPE.VOTE) {
                const ind = this.props.findComponentInd(groupId, this.props.component.component.nextState);
                //assumed that input stored after text
                let componentObj = this.props.components[groupId][ind];
                componentObj.component.nextState = this.state.nextState;
                this.props.onChange(componentObj);
            }
            this.props.onChange(textComponentObj);
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

    createButton(row, toBegin = false) {
        const {intl} = this.props;
        let buttons = this.state.buttonsGroup.buttons.slice();
        const buttonObj = {
            intent: "default",
            nextState: this.props.component.component.id,
            text: intl.formatMessage({id: 'app.constructor.component.button'}),
            value: intl.formatMessage({id: 'app.constructor.component.button'})
        };
        if (row === buttons.length) {
            buttons.push([buttonObj]);
        } else if (row === -1) {
            buttons.unshift([buttonObj]);
        } else {
            if (toBegin) {
                buttons[row].unshift(buttonObj);
            } else {
                buttons[row].push(buttonObj);
            }
        }
        this.state.buttonsGroup.buttons = buttons;
        this.setState({buttonsGroup: this.state.buttonsGroup});
    }

    renderButton(elem, row, col) {
        return (
            <Button label={elem.text} className={"button-elem"} onClick={() => {
                this.settingsButtonDialog.getWrappedInstance().onShow(elem, row, col, this.findMessage(elem.nextState), Number(this.props.match.params.groupId))
            }}/>
        );
    }

    createButtonsList() {
        if (!this.state.buttonsGroup) {
            return null;
        }

        let renderedMatrix = [];
        for (let i = 0; i < this.state.buttonsGroup.buttons.length; i++) {
            let renderedRow = [];
            if (this.state.buttonsGroup.buttons[i].length < 5) {
                renderedRow.push(<Button icon='pi pi-plus' onClick={() => this.createButton(i, true)}/>);
            }
            for (let j = 0; j < this.state.buttonsGroup.buttons[i].length; j++) {
                renderedRow.push(this.renderButton(this.state.buttonsGroup.buttons[i][j], i, j));
            }
            if (this.state.buttonsGroup.buttons[i].length < 5) {
                renderedRow.push(<Button icon='pi pi-plus' onClick={() => this.createButton(i)}/>);
            }
            renderedMatrix.push(<div>{renderedRow}</div>);
        }
        if (this.state.buttonsGroup.buttons.length > 0) {
            renderedMatrix.push(<div><Button icon='pi pi-plus' className={"button-elem"}
                                             onClick={() => this.createButton(this.state.buttonsGroup.buttons.length)}/>
            </div>);
        }
        return renderedMatrix;
    }

    checkEmptyRows() {
        if (!this.state.buttonsGroup) {
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
        const ind = this.props.findComponentInd(Number(this.props.match.params.groupId), idBtn);
        if (ind !== -1) {
            const component = this.props.components[Number(this.props.match.params.groupId)][ind];
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
        const {intl} = this.props;

        const groupId = Number(this.props.match.params.groupId);
        const componentId = Number(this.props.match.params.componentId);

        const lastInVote = this.props.groups[groupId].type === BotConstructor.GROUP_TYPE.VOTE &&
            this.props.components[groupId].length > 0 &&
            this.props.components[groupId][this.props.components[groupId].length - 2].component.id === componentId;

        return (
            <div className={"text-card"}>
                <Growl ref={(el) => this.growl = el}/>
                <div className={"text-card_detail-element"}>
                    <InputText value={this.state.title} onChange={(e) => this.setState({title: e.target.value})}
                               placeholder={intl.formatMessage({id: 'app.dialog.name'})}/>
                </div>
                <div className={"text-card_detail-element"}>
                    <InputTextarea rows={5} cols={60} value={this.state.text}
                                   onChange={(e) => this.setState({text: e.target.value})}
                                   placeholder={intl.formatMessage({id: 'app.constructor.message.text'})}/>
                </div>
                <div className={"text-card_detail-element"}>
                    {lastInVote &&
                    <Dropdown value={this.state.nextState} options={nextComponentList} editable={true}
                              onChange={(e) => this.setState({nextState: e.value})}
                              placeholder="Select next component"/>}
                </div>
                <div className="text-card_button-panel">
                    <Button label={intl.formatMessage({id: "app.bot.remove"})}
                            onClick={() => this.props.onRemove(this.props.component)}/>
                </div>
                {this.props.component.component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INFO &&
                this.props.component.buttonsGroup &&
                <div>
                    <div className={"button-panel"}>
                        <div>
                            <Button icon='pi pi-plus' className={"button-elem"} onClick={() => this.createButton(-1)}/>
                        </div>
                        {buttonsList}
                    </div>
                    <ButtonSettingsDialog nextComponentList={nextComponentList}
                                          botSchemeId={Number(this.props.match.params.id)}
                                          removeComponent={this.props.onRemove}
                                          ref={(obj) => this.settingsButtonDialog = obj}
                                          id={this.props.component.component.id}
                                          appendComponent={this.props.appendComponent}
                                          saveButton={this.saveButton}
                                          removeButton={this.removeButton}
                                          onChange={this.props.onChange}/>
                </div>}
            </div>
        );
    }
}

export default withRouter(injectIntl(ComponentSettings, {withRef: true}));
