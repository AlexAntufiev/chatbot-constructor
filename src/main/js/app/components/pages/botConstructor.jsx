import React, {Component} from 'react';
import {Button} from 'primereact/button';
import ComponentGroup from "app/components/constructor/componentGroup";
import * as BuilderService from "app/service/builder";
import makeTemplateStr from "app/utils/makeTemplateStr";
import * as routes from "app/constants/routes";
import {withRouter} from "react-router";
import {injectIntl} from "react-intl";
import ComponentSettings from "app/components/constructor/componentSettings";

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

        switch (type) {
            case BotConstructor.COMPONENT_TYPES.BUTTON_GROUP:
                componentObj.buttonsGroup = {buttons: []};
                componentObj.component.type = BotConstructor.COMPONENT_SCHEME_TYPES.INFO;
                componentObj.component.title = "Button group";
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
                        nextState: componentObj.component.id,
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
            let components = Object.assign({}, this.state.components);
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
        let components = Object.assign({}, this.state.components);
        const ind = this.findComponentInd(groupId, componentObj.component.id);
        if (ind !== -1) {
            components[groupId].splice(ind, 1);
            this.setState({components: components});
        }
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
                components.push(this.state.components[groupId][i]);
            }
        }
        console.log(components);
        BuilderService.saveBotScheme(this.props.match.params.id, components);
    }

    render() {
        const groupId = 0;
        const idComponent = this.findComponentInd(groupId, Number(this.props.match.params.componentId));
        const component = idComponent === -1 ? null : this.state.components[groupId][idComponent];

        return (<div>
            <div className={"constructor-page p-grid"}>
                <div className={"constructor-container p-col-7"}>
                    <ComponentGroup title={"Group1"} onCreateComponent={this.createComponent}
                                    botSchemeId={Number(this.props.match.params.id)}
                                    components={this.state.components[groupId]}
                                    groupId={groupId}/>
                    {/*<Button label={"Append group"} icon='pi pi-plus' onClick={this.refreshScheme}/>*/}
                    <Button label={"Save"} icon='pi pi-plus' onClick={this.saveScheme}/>
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