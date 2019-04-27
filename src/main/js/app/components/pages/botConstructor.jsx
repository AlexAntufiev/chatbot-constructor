import React, {Component} from 'react';
import {Fieldset} from 'primereact/fieldset';
import {Inplace, InplaceContent, InplaceDisplay} from 'primereact/components/inplace/Inplace';
import {InputText} from 'primereact/inputtext';
import {Button} from 'primereact/button';
import {TieredMenu} from 'primereact/tieredmenu';
import {Dropdown} from 'primereact/dropdown';
import {InputTextarea} from "primereact/inputtextarea";

export default class BotConstructor extends Component {

    render() {
        const items = [
            {
                label: "Text message",
                icon: "pi pi-comment"
            },
            {
                label: "Button",
                icon: "pi pi-folder"
            },
            {
                label: "Button group",
                icon: "pi pi-th-large"
            },
            {
                label: "Vote list",
                icon: "pi pi-list"
            },
            {
                label: "User input",
                icon: "pi pi-ellipsis-h"
            }
        ];

        const nextComponents = [
            {name: 'Text message', code: '0'},
            {name: 'Button', code: '1'},
            {name: 'Button group', code: '2'},
            {name: 'Vote list', code: '3'},
            {name: 'User input', code: '4'}
        ];

        return (<div>
            <div className={"constructor-page p-grid"}>
                <div className={"constructor-container p-col-7"}>
                    <Fieldset legend={<div className={"p-grid p-align-baseline"}><Inplace ref={el => this.panel1 = el}>
                        <InplaceDisplay>
                            Group Name
                        </InplaceDisplay>
                        <InplaceContent>
                            <InputText autoFocus value={"Group Name"} onBlur={event => this.panel1.close()}/>
                        </InplaceContent>
                    </Inplace>
                        <TieredMenu model={items} popup={true} ref={el => this.menu = el}/>
                        <Button icon="pi pi-plus" onClick={(event) => this.menu.toggle(event)}/>
                    </div>}>
                        <div className={"p-grid"}>
                            <Button icon={"pi pi-comment"} label={"Text message"}
                                    className={"container-group-element"}/>
                            <Button icon={"pi pi-folder"} label={"Button"} className={"container-group-element"}/>
                            <Button icon={"pi pi-th-large"} label={"Button group"}
                                    className={"container-group-element"}/>
                            <Button icon={"pi pi-list"} label={"Vote list"} className={"container-group-element"}/>
                            <Button icon={"pi pi-ellipsis-h"} label={"User input"}
                                    className={"container-group-element"}/>
                        </div>
                    </Fieldset>

                    <Fieldset legend={<div className={"p-grid p-align-baseline"}><Inplace ref={el => this.panel2 = el}>
                        <InplaceDisplay>
                            Group Name
                        </InplaceDisplay>
                        <InplaceContent>
                            <InputText autoFocus value={"Group Name"} onBlur={event => this.panel2.close()}/>
                        </InplaceContent>
                    </Inplace>
                        <TieredMenu model={items} popup={true} ref={el => this.menu = el}/>
                        <Button icon="pi pi-plus" onClick={(event) => this.menu.toggle(event)}/>
                    </div>}>
                        <div className={"p-grid"}>
                            <Button icon={"pi pi-comment"} label={"Text message"}
                                    className={"container-group-element"}/>
                            <Button icon={"pi pi-folder"} label={"Button"} className={"container-group-element"}/>
                            <Button icon={"pi pi-th-large"} label={"Button group"}
                                    className={"container-group-element"}/>
                            <Button icon={"pi pi-list"} label={"Vote list"} className={"container-group-element"}/>
                            <Button icon={"pi pi-ellipsis-h"} label={"User input"}
                                    className={"container-group-element"}/>
                        </div>
                    </Fieldset>
                    <Button label={"Append group"} icon='pi pi-plus'/>
                </div>
                <div className={"constructor-component-settings p-col-5"}>
                    <div className={"text-card"}>
                        <div className={"text-card_detail-element"}>
                            <InputText value={"Component Name"}/>
                        </div>
                        <div className={"text-card_detail-element"}>
                            <InputTextarea rows={5} cols={60}/>
                        </div>
                        <div className={"text-card_detail-element"}>
                            <Dropdown optionLabel="name" options={nextComponents} placeholder="Select next component"/>
                        </div>
                        <div className="text-card_button-panel">
                            <Button label={"Save"}/>
                            <Button label={"Remove"}/>
                        </div>
                    </div>
                </div>
            </div>
        </div>);
    }
}
