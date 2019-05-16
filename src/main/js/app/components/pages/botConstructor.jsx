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
import {TieredMenu} from "primereact/tieredmenu";

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

    static get GROUP_TYPE() {
        return {
            DEFAULT: 0,
            VOTE: 1
        };
    }

    static get SCHEME_ACTION_TYPE() {
        return {
            STORE_VOTE_ENTRY: 0,
            PERSIST_VOTE_TO_TABLE: 1
        };
    }

    constructor(props) {
        super(props);

        this.state = {
            components: {},
            groups: {},
            removedGroups: []
        };

        this.groupTypeMenuItems = [
            {
                label: 'Обычная',
                command: () => {
                    this.addGroup(BotConstructor.GROUP_TYPE.DEFAULT);
                    this.menu.hide();
                }
            },
            {
                label: 'Опрос',
                command: () => {
                    this.addGroup(BotConstructor.GROUP_TYPE.VOTE);
                    this.menu.hide();
                }
            },
        ];
        this.createComponent = this.createComponent.bind(this);
        this.refreshScheme = this.refreshScheme.bind(this);
        this.removeComponent = this.removeComponent.bind(this);
        this.changeComponent = this.changeComponent.bind(this);
        this.saveScheme = this.saveScheme.bind(this);
        this.findComponentInd = this.findComponentInd.bind(this);
        this.appendComponent = this.appendComponent.bind(this);
        this.createGroupsList = this.createGroupsList.bind(this);
        this.addGroup = this.addGroup.bind(this);
        this.removeGroup = this.removeGroup.bind(this);
        this.updateGroup = this.updateGroup.bind(this);
        this.isShadowInput = this.isShadowInput.bind(this);
        this.createButtonGroup = this.createButtonGroup.bind(this);
        this.createUserInput = this.createUserInput.bind(this);
    }

    createButtonGroup(componentObj) {
        BuilderService.newComponent(Number(this.props.match.params.id), (res) => {
            const groupId = componentObj.component.groupId;
            let shadowInput = {
                buttonsGroup: null,
                component: {
                    id: res.data.payload.componentId,
                    groupId: groupId,
                    nextState: componentObj.component.id,
                    schemeId: Number(this.props.match.params.id),
                    text: null,
                    title: "Shadow input",
                    type: BotConstructor.COMPONENT_SCHEME_TYPES.INPUT
                }
            };
            componentObj.component.nextState = res.data.payload.componentId;
            let components = this.state.components;
            components[groupId].push(componentObj);
            components[groupId].push(shadowInput);
            this.setState({components: components});
            const url = makeTemplateStr(routes.botConstructorComponent(), {
                id: this.props.match.params.id,
                groupId: groupId,
                componentId: componentObj.component.id
            });
            this.props.history.push(url);
        }, null, this);
    }


    //applicable for votes only
    createUserInput(componentObj) {
        BuilderService.newComponent(Number(this.props.match.params.id), (res) => {
            const groupId = componentObj.component.groupId;
            let inputElem = {
                buttonsGroup: null,
                component: {
                    id: Number(res.data.payload.componentId),
                    type: BotConstructor.COMPONENT_SCHEME_TYPES.INPUT,
                    groupId: groupId,
                    nextState: null,
                    title: "Shadow user input",
                    schemeId: Number(this.props.match.params.id),
                },
                actions: [
                    {
                        type: BotConstructor.SCHEME_ACTION_TYPE.STORE_VOTE_ENTRY
                    },
                    {
                        type: BotConstructor.SCHEME_ACTION_TYPE.PERSIST_VOTE_TO_TABLE
                    }
                ]

            };
            componentObj.component.nextState = inputElem.component.id;


            if (this.state.components[groupId].length > 0) {
                let lastComponent = this.state.components[groupId][this.state.components[groupId].length - 1];
                lastComponent.actions = [{type: BotConstructor.SCHEME_ACTION_TYPE.STORE_VOTE_ENTRY}];
                lastComponent.component.nextState = componentObj.component.id;
            }
            this.state.components[groupId].push(componentObj);
            this.state.components[groupId].push(inputElem);
            this.setState({components: this.state.components});

            const url = makeTemplateStr(routes.botConstructorComponent(), {
                id: this.props.match.params.id,
                groupId: groupId,
                componentId: componentObj.component.id
            });
            this.props.history.push(url);
        }, null, this);
    }

    createComponent(id, type, groupId) {
        let componentObj = {
            buttonsGroup: null,
            component: {
                id: Number(id),
                groupId: groupId,
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
                this.createButtonGroup(componentObj);
                break;
            case BotConstructor.COMPONENT_TYPES.USER_INPUT:
                //Text message for user input
                componentObj.component.type = BotConstructor.COMPONENT_SCHEME_TYPES.INFO;
                componentObj.component.title = intl.formatMessage({id: 'app.constructor.component.userinput'});
                this.createUserInput(componentObj);
                break;
        }
    }

    appendComponent(component) {
        this.state.components[component.component.groupId].push(component);
        this.setState({components: this.state.components});
    }

    refreshScheme() {
        BuilderService.getGroups(this.props.match.params.id, (res) => {
            let components = {};
            let groups = {};
            res.data.payload.forEach((group) => {
                groups[group.id] = group;
                components[group.id] = [];
            });
            BuilderService.getBotScheme(this.props.match.params.id, (res) => {

                res.data.payload.forEach((component) => {
                    components[component.component.groupId].push(component);
                });

                this.setState({
                    removedGroups: [],
                    components: components,
                    groups: groups
                });
            }, null, this);

        }, null, this);
    }

    componentDidMount() {
        this.refreshScheme();
    }

    findComponentInd(groupId, componentId) {
        if (!componentId || !groupId) {
            return -1;
        }
        groupId = Number(groupId);
        componentId = Number(componentId);
        if (!Array.isArray(this.state.components[groupId])) {
            return -1;
        }
        for (let i = 0; i < this.state.components[groupId].length; i++) {
            const componentObj = this.state.components[groupId][i];
            if (componentObj.component.id === componentId) {
                return i;
            }
        }
        return -1;
    }

    removeComponent(componentObj) {
        const groupId = componentObj.component.groupId;
        const ind = this.findComponentInd(groupId, componentObj.component.id);
        if (ind === -1) {
            return;
        }

        if (this.state.groups[groupId].type === BotConstructor.GROUP_TYPE.VOTE) {

            //if last in vote list
            if (ind === this.state.components[groupId].length - 2) {
                if (this.state.components[groupId].length > 2) {
                    this.state.components[groupId][ind - 1].actions = [{
                            type: BotConstructor.SCHEME_ACTION_TYPE.STORE_VOTE_ENTRY
                        },
                        {
                            type: BotConstructor.SCHEME_ACTION_TYPE.PERSIST_VOTE_TO_TABLE
                        }];
                }
            }
            if (ind - 1 > 0) {
                if (ind + 2 < this.state.components[groupId].length) {
                    this.state.components[groupId][ind - 1].component.nextState = this.state.components[groupId][ind + 2].component.id;
                }
            }
            this.state.components[groupId].splice(ind, 1); //remove text
            this.state.components[groupId].splice(ind, 1); //remove input
        } else {
            this.state.components[groupId].splice(ind, 1);

            if (componentObj.buttonsGroup) {
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
                            if (nextElem.component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INFO && !nextElem.buttonsGroup) {
                                this.state.components[groupId].splice(nextElemInd, 1);
                            }
                        }
                    }
                }
            }
        }
        for (let groupId in this.state.components) {
            for (let i = 0; i < this.state.components[groupId].length; i++) {
                const elem = this.state.components[groupId][i];
                if (elem.component.nextState === componentObj.component.id) {
                    elem.component.nextState = elem.component.id;
                }
                if (elem.buttonsGroup) {
                    for (let row = 0; row < elem.buttonsGroup.buttons.length; row++) {
                        for (let col = 0; col < elem.buttonsGroup.buttons[row].length; col++) {
                            if (elem.buttonsGroup.buttons[row][col].nextState === componentObj.component.id) {
                                if (this.state.groups[groupId].type === BotConstructor.GROUP_TYPE.VOTE && this.state.components[groupId].length > 0) {
                                    elem.buttonsGroup.buttons[row][col].nextState =  this.state.components[groupId][0].component.id;
                                } else {
                                    elem.buttonsGroup.buttons[row][col].nextState = elem.component.id;
                                }
                            }
                        }
                    }
                }
            }
        }
        this.setState({components: this.state.components});
    }

    changeComponent(componentObj) {
        if (!componentObj) {
            return;
        }
        const groupId = componentObj.component.groupId;
        let components = this.state.components;
        const ind = this.findComponentInd(groupId, componentObj.component.id);
        if (ind !== -1) {
            components[groupId][ind].component.title = componentObj.component.title;
            components[groupId][ind].component.text = componentObj.component.text;
            components[groupId][ind].component.nextState = componentObj.component.nextState;
            this.setState({components: components});
        }
    }

    saveScheme() {
        let components = [];
        for (let groupId in this.state.components) {
            for (let i = 0; i < this.state.components[groupId].length; i++) {
                if (this.state.components[groupId][i].component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INFO) {
                    const {intl} = this.props;
                    if (this.state.components[groupId][i].buttonsGroup && this.state.components[groupId][i].buttonsGroup.buttons.length === 0) {
                        const text = makeTemplateStr(intl.formatMessage({id: 'app.constructor.error.empty.buttons.template'}),
                            {title: this.state.components[groupId][i].component.title});
                        AxiosMessages.customError(this, text);
                        return;
                    }
                    if (this.state.components[groupId][i].component.text.trim() === '') {
                        const text = makeTemplateStr(intl.formatMessage({id: 'app.constructor.error.fill.text.template'}),
                            {title: this.state.components[groupId][i].component.title});
                        AxiosMessages.customError(this, text);
                        return;
                    }
                }
                components.push(this.state.components[groupId][i]);
            }
        }
        BuilderService.saveBotScheme(this.props.match.params.id, components, () => {
            AxiosMessages.successOperation(this, 'app.constructor.scheme.saved');
            BuilderService.removeGroups(this.props.match.params.id, this.state.removedGroups,
                () => this.setState({removedGroups: []}),
                null, this);
        }, null, this);
    }

    removeGroup(groupId) {
        while (this.state.components[groupId].length > 0) {
            this.removeComponent(this.state.components[groupId][0]);
        }
        delete this.state.components[groupId];
        delete this.state.groups[groupId];
        this.state.removedGroups.push(groupId);
        this.setState({
            components: this.state.components,
            groups: this.state.groups,
            removedGroups: this.state.removedGroups
        });
    }

    createGroupsList() {
        let groups = [];
        for (let groupId in this.state.groups) {
            groups.push(<ComponentGroup group={this.state.groups[groupId]} onCreateComponent={this.createComponent}
                                        botSchemeId={Number(this.props.match.params.id)}
                                        components={this.state.components[groupId]} removeGroup={this.removeGroup}
                                        updateGroup={this.updateGroup}/>);
        }
        return groups;
    }

    addGroup(type) {
        BuilderService.addGroup(this.props.match.params.id, type === BotConstructor.GROUP_TYPE.DEFAULT ? 'New group' : 'New vote', type, (res) => {
            this.state.groups[res.data.payload.id] = res.data.payload;
            this.state.components[res.data.payload.id] = [];
            this.setState({
                groups: this.state.groups,
                components: this.state.components
            });
        }, null, this);
    }

    updateGroup(group) {
        this.state.groups[group.id] = group;
        this.setState({groups: this.state.groups});
    }

    isShadowInput(component) {
        if (!component) {
            return false;
        }

        if (component.component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INFO) {
            return false;
        }
        for (let i = 0; i < this.state.components[component.component.groupId].length; i++) {
            const tmpComp = this.state.components[component.component.groupId][i];
            if (tmpComp.component.nextState === component.component.id && tmpComp.buttonsGroup) {
                return true;
            }
        }
        return false;
    }

    render() {
        const groupId = Number(this.props.match.params.groupId);
        const componentId = Number(this.props.match.params.componentId);

        const idComponent = this.findComponentInd(groupId, componentId);
        const component = idComponent === -1 ? null : this.state.components[groupId][idComponent];
        const groupsList = this.createGroupsList();
        const {intl} = this.props;

        return (<div>
            <Growl ref={(el) => this.growl = el}/>
            <div className={"constructor-page p-grid"}>
                <div className={"constructor-container p-col-7"}>
                    {groupsList}
                    <div className={'button-list-elem'}>
                        <TieredMenu model={this.groupTypeMenuItems} popup={true} ref={el => this.menu = el}/>
                        <Button label={intl.formatMessage({id: 'app.constructor.scheme.add.group'})} icon='pi pi-plus'
                                onClick={(e) => this.menu.toggle(e)}/>
                    </div>
                    <div className={'button-list-elem'}>
                        <Button label={intl.formatMessage({id: "app.dialog.save"})} icon='pi pi-plus'
                                onClick={this.saveScheme}/>
                    </div>
                </div>
                <div className={"constructor-component-settings p-col-5"}>
                    <ComponentSettings components={this.state.components}
                                       onRemove={this.removeComponent} onChange={this.changeComponent}
                                       component={component} appendComponent={this.appendComponent}
                                       findComponentInd={this.findComponentInd} groups={this.state.groups}
                                       isShadowInput={this.isShadowInput}/>
                </div>
            </div>
        </div>);
    }
}

export default withRouter(injectIntl(BotConstructor, {withRef: true}));
