import React, {Component} from 'react';
import {Button} from 'primereact/button';
import ComponentGroup from "app/components/constructor/componentGroup";
import * as BuilderService from "app/service/builder";
import makeTemplateStr from "app/utils/makeTemplateStr";
import * as routes from "app/constants/routes";
import {withRouter} from "react-router";
import {injectIntl} from "react-intl";
import ComponentSettings from "app/components/constructor/componentSettings";
import {Growl} from "primereact/growl";
import * as AxiosMessages from 'app/utils/axiosMessages';

class BotConstructor extends Component {

    static get COMPONENT_TYPES() {
        return {
            VOTE_LIST: 0,
            BUTTON_GROUP: 1,
            USER_INPUT: 2
        }
    };

    static get COMPONENT_SCHEME_TYPES() {
        return {
            INPUT: 0,
            INFO: 1,
            RESET: 2
        };
    }

    constructor(props) {
        super(props);

        this.state = {
            components: {0: []}
        };
        this.createComponent = this.createComponent.bind(this);
        this.refreshScheme = this.refreshScheme.bind(this);
        this.removeComponent = this.removeComponent.bind(this);
        this.changeComponent = this.changeComponent.bind(this);
        this.saveScheme = this.saveScheme.bind(this);
        this.findComponentInd = this.findComponentInd.bind(this);
        this.appendComponent = this.appendComponent.bind(this);
    }

    createComponent(id, type) {
        let componentObj = {
            buttonsGroup: null,
            component: {
                id: Number(id),
                groupId: 0,
                nextState: Number(id),
                schemeId: Number(this.props.match.params.id),
                text: "",
                title: ""
            }
        };
        const {intl} = this.props;

        switch (type) {
            case BotConstructor.COMPONENT_TYPES.BUTTON_GROUP:
                componentObj.buttonsGroup = {buttons: []};
                componentObj.component.type = BotConstructor.COMPONENT_SCHEME_TYPES.INFO;
                componentObj.component.title = intl.formatMessage({id: 'app.constructor.component.buttongroup'});
                break;
            case BotConstructor.COMPONENT_TYPES.USER_INPUT:
                componentObj.component.type = BotConstructor.COMPONENT_SCHEME_TYPES.INPUT;
                componentObj.component.title = "User input";
                break;
            case BotConstructor.COMPONENT_TYPES.VOTE_LIST:
                //process vote list
                break;
        }

        const appendFunc = (shadowInput) => {
            //hardcode for one group
            let components = Object.assign({}, this.state.components);
            components[0].push(componentObj);
            shadowInput && components[0].push(shadowInput);
            this.setState({components: components});
            const url = makeTemplateStr(routes.botConstructorComponent(), {
                id: this.props.match.params.id,
                groupId: 0,
                componentId: componentObj.component.id
            });
            this.props.history.push(url);
        };

        if (componentObj.buttonsGroup) {
            BuilderService.newComponent(Number(this.props.match.params.id), (res) => {
                let shadowInput = {
                    buttonsGroup: null,
                    component: {
                        id: res.data.payload.componentId,
                        groupId: 0,
                        nextState: null,
                        schemeId: Number(this.props.match.params.id),
                        text: null,
                        title: "Shadow input",
                        type: BotConstructor.COMPONENT_SCHEME_TYPES.INPUT
                    }
                };
                componentObj.component.nextState = res.data.payload.componentId;
                appendFunc(shadowInput);
            }, null, this);
        } else {
            appendFunc();
        }
    }

    appendComponent(component) {
        this.state.components[0].push(component);
        this.setState({components: this.state.components});
    }

    refreshScheme() {
        BuilderService.getBotScheme(this.props.match.params.id, (res) => {
            let components = {0: []};
            res.data.payload.forEach((component, ind) => {
                //hardcode for one group
                components[0].push(component);
            });
            this.setState({components: components});
        }, null, this);
    }

    componentDidMount() {
        this.refreshScheme();
    }

    findComponentInd(groupId, componentId) {
        if (!componentId) {
            return -1;
        }
        groupId = Number(groupId);
        componentId = Number(componentId);
        for (let i = 0; i < this.state.components[groupId].length; i++) {
            const componentObj = this.state.components[groupId][i];
            if (componentObj.component.id === componentId) {
                return i;
            }
        }
        return -1;
    }

    removeComponent(componentObj, groupId) {
        const ind = this.findComponentInd(groupId, componentObj.component.id);
        if (ind !== -1) {
            this.state.components[groupId].splice(ind, 1);
            const shadowInpInd = this.findComponentInd(groupId, componentObj.component.nextState);
            if (shadowInpInd !== -1) {
                if (this.state.components[groupId][shadowInpInd].component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INPUT) {
                    this.state.components[groupId].splice(shadowInpInd, 1);
                }
            }
        }
        if (componentObj.buttonsGroup) {
            for (let i = 0; i < componentObj.buttonsGroup.buttons.length; i++) {
                for (let j = 0; j < componentObj.buttonsGroup.buttons[i].length; j++) {
                    const btn = componentObj.buttonsGroup.buttons[i][j];
                    const nextElemInd = this.findComponentInd(groupId, btn.nextState);
                    if (nextElemInd !== -1) {
                        const nextElem = this.state.components[groupId][nextElemInd];
                        if (nextElem.component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INPUT && !nextElem.buttonsGroup) {
                            this.state.components[groupId].splice(nextElemInd, 1);
                        }
                    }
                }
            }
        }

        for (let groupId in this.state.components) {
            for (let i = 0; i < this.state.components[groupId].length; i++) {
                const elem = this.state.components[groupId][i];
                if (elem.component.nextState === componentObj.component.id) {
                    elem.component.nextState = null;
                }
                if (elem.buttonsGroup) {
                    for (let row = 0; row < elem.buttonsGroup.buttons.length; row++) {
                        for (let col = 0; col < elem.buttonsGroup.buttons[row].length; col++) {
                            if (elem.buttonsGroup.buttons[row][col].nextState === componentObj.component.id) {
                                elem.buttonsGroup.buttons[row][col].nextState = null;
                            }
                        }
                    }
                }
            }
        }
        this.setState({components: this.state.components});
    }

    changeComponent(componentObj, groupId) {
        let components = Object.assign({}, this.state.components);
        const ind = this.findComponentInd(groupId, componentObj.component.id);

        components[groupId][ind].component.title = componentObj.component.title;
        components[groupId][ind].component.text = componentObj.component.text;
        components[groupId][ind].component.nextState = componentObj.component.nextState;
        this.setState({components: components});
    }

    saveScheme() {
        let components = [];
        for (let groupId in this.state.components) {
            for (let i = 0; i < this.state.components[groupId].length; i++) {
                if (this.state.components[groupId][i].component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INFO) {
                    const {intl} = this.props;
                    if (this.state.components[groupId][i].buttonsGroup && this.state.components[groupId][i].buttonsGroup.buttons.length === 0) {
                        AxiosMessages.customError(this, intl.formatMessage({id: 'app.constructor.error.empty.buttons'}));
                        return;
                    }
                    if (this.state.components[groupId][i].component.text.trim() === '') {
                        AxiosMessages.customError(this, intl.formatMessage({id: 'app.constructor.error.fill.text'}));
                        return;
                    }
                }
                components.push(this.state.components[groupId][i]);
            }
        }
        BuilderService.saveBotScheme(this.props.match.params.id, components, () => AxiosMessages.successOperation(this, 'app.constructor.scheme.saved'), null, this);
    }

    render() {
        const groupId = 0;
        const idComponent = this.findComponentInd(groupId, Number(this.props.match.params.componentId));
        const component = idComponent === -1 ? null : this.state.components[groupId][idComponent];
        const {intl} = this.props;

        return (<div>
            <Growl ref={(el) => this.growl = el}/>
            <div className={"constructor-page p-grid"}>
                <div className={"constructor-container p-col-7"}>
                    <ComponentGroup title={"Group1"} onCreateComponent={this.createComponent}
                                    botSchemeId={Number(this.props.match.params.id)}
                                    components={this.state.components[groupId]}
                                    groupId={groupId}/>
                    {/*<Button label={"Append group"} icon='pi pi-plus' onClick={this.refreshScheme}/>*/}
                    <Button label={intl.formatMessage({id: "app.dialog.save"})} icon='pi pi-plus'
                            onClick={this.saveScheme}/>
                </div>
                <div className={"constructor-component-settings p-col-5"}>
                    <ComponentSettings components={this.state.components}
                                       onRemove={this.removeComponent} onChange={this.changeComponent} groupId={groupId}
                                       component={component} appendComponent={this.appendComponent}
                                       findComponentInd={this.findComponentInd}/>
                </div>
            </div>
        </div>);
    }
}

export default withRouter(injectIntl(BotConstructor, {withRef: true}));
