import React, {Component} from "react";
import {InputText} from "primereact/inputtext";
import {InputTextarea} from "primereact/inputtextarea";
import {Dropdown} from "primereact/dropdown";
import {Button} from "primereact/button";
import {withRouter} from "react-router";
import {injectIntl} from "react-intl";
import BotConstructor from "app/components/pages/botConstructor";

class ComponentSettings extends Component {
    constructor(props) {
        super(props);

        this.state = {
            title: '',
            text: '',
            nextState: 0
        };

        this.createNextStatesList = this.createNextStatesList.bind(this);
    }

    createNextStatesList() {
        let nextComponents = [];
        if (this.props.startComponent) {
            nextComponents.push({
                label: this.props.startComponent.component.title,
                value: this.props.startComponent.component.id
            });
        }
        for (let groupId in this.props.components) {
            const group = this.props.components[groupId];
            for (let i = 0; i < group.length; i++) {
                const componentObj = group[i];
                console.log(componentObj);
                if (componentObj.component.type === BotConstructor.COMPONENT_SCHEME_TYPES.INFO && !componentObj.buttonsGroup) {
                    continue;
                }
                if (componentObj.component.id === Number(this.props.component.component.id)) {
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
                nextState: this.props.component.component.nextState
            });
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

    render() {
        if (!this.props.component) {
            return (<div/>);
        }

        const nextComponentList = this.createNextStatesList();
        return (
            <div className={"text-card"}>
                <div className={"text-card_detail-element"}>
                    <InputText value={this.state.title} onChange={(e) => this.setState({title: e.target.value})}/>
                </div>
                <div className={"text-card_detail-element"}>
                    <InputTextarea rows={5} cols={60} value={this.state.text}
                                   onChange={(e) => this.setState({text: e.target.value})}/>
                </div>
                <div className={"text-card_detail-element"}>
                    <Dropdown value={this.state.nextState} options={nextComponentList}
                              onChange={(e) => this.setState({nextState: e.value})}
                              editable={true} placeholder="Select next component"/>
                </div>
                <div className="text-card_button-panel">
                    <Button label={"Remove"}
                            onClick={() => this.props.onRemove(this.props.component, this.props.groupId)}/>
                </div>
            </div>
        );
    }
}

export default withRouter(injectIntl(ComponentSettings, {withRef: true}));
