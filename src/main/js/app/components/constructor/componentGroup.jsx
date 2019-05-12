import React, {Component} from "react";
import {Fieldset} from "primereact/fieldset";
import {Inplace, InplaceContent, InplaceDisplay} from "primereact/components/inplace/Inplace";
import {InputText} from "primereact/inputtext";
import {TieredMenu} from "primereact/tieredmenu";
import {Button} from "primereact/button";
import {injectIntl} from "react-intl";
import {withRouter} from "react-router";
import * as BuilderService from "app/service/builder";
import BotConstructor from "app/components/pages/botConstructor";
import {Growl} from "primereact/growl";
import * as routes from "app/constants/routes";
import makeTemplateStr from "app/utils/makeTemplateStr";

class ComponentGroup extends Component {
    constructor(props) {
        super(props);
        const {intl} = this.props;
        this.items = [
            {
                label: intl.formatMessage({id: "app.constructor.component.buttongroup"}),
                icon: "pi pi-th-large",
                command: () => {
                    BuilderService.newComponent(this.props.botSchemeId, (res) => {
                        this.props.onCreateComponent(res.data.payload.componentId, BotConstructor.COMPONENT_TYPES.BUTTON_GROUP);
                        this.menu.hide();
                    }, null, this);
                }
            },
            /* {
                 label: "Vote list",
                 icon: "pi pi-list",
                 command: () => {
                     BuilderService.newComponent(this.props.botSchemeId, (res) => {
                         this.props.onCreateComponent(res.data.payload.componentId, BotConstructor.COMPONENT_TYPES.VOTE_LIST);
                         this.menu.hide();
                     }, null, this);
                 }
             },
             {
                 label: "User input",
                 icon: "pi pi-ellipsis-h",
                 command: () => {
                     BuilderService.newComponent(this.props.botSchemeId, (res) => {
                         this.props.onCreateComponent(res.data.payload.componentId, BotConstructor.COMPONENT_TYPES.USER_INPUT);
                         this.menu.hide();
                     }, null, this);
                 }
             }*/
        ];

        this.createComponentList = this.createComponentList.bind(this);
    }

    createComponentList() {
        if (!Array.isArray(this.props.components)) {
            return;
        }
        let renderedList = [];
        for (let i = 0; i < this.props.components.length; i++) {
            const componentObj = this.props.components[i];
            let icon = "";
            let label = componentObj.component.title;
            switch (componentObj.component.type) {
                /*case BotConstructor.COMPONENT_SCHEME_TYPES.INPUT:
                    icon = "pi pi-ellipsis-h";
                    break;*/
                case BotConstructor.COMPONENT_SCHEME_TYPES.INFO:
                    if (componentObj.buttonsGroup) {
                        icon = "pi pi-th-large";
                    }
                    break;
            }
            if (icon !== "" && label !== "") {
                renderedList.push(<Button icon={icon} label={label} className={"container-group-element"}
                                          onClick={() => {
                                              const url = makeTemplateStr(routes.botConstructorComponent(), {
                                                  id: this.props.botSchemeId,
                                                  groupId: this.props.groupId,
                                                  componentId: componentObj.component.id
                                              });
                                              this.props.history.push(url);
                                          }
                                          }/>);
            }
        }
        return renderedList;
    }

    render() {
        const componentList = this.createComponentList();

        return (
            <Fieldset legend={<div className={"p-grid p-align-baseline"}><Inplace ref={el => this.panel = el}>
                <Growl ref={(el) => this.growl = el}/>
                <InplaceDisplay>
                    {this.props.title}
                </InplaceDisplay>
                <InplaceContent>
                    <InputText autoFocus value={this.props.title} onBlur={event => this.panel.close()}/>
                </InplaceContent>
            </Inplace>
                <TieredMenu model={this.items} popup={true} ref={el => this.menu = el}/>
                <Button icon="pi pi-plus" onClick={(event) => this.menu.toggle(event)}/>
            </div>}>
                <div className={"p-grid"}>
                    {componentList}
                </div>
            </Fieldset>
        );
    }
}

export default withRouter(injectIntl(ComponentGroup, {withRef: true}));