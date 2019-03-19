import React, {Component} from 'react';
import {Button} from "primereact/button"
import TextMessage from "app/components/constructor/textMessage"

export default class BotBroadcastingDetail extends Component {

    render() {

        return (<div className="p-grid p-align-start bot-broadcasting">
            <div className="p-col bot-broadcasting_elements-container">
                <div className="bot-broadcasting_elements-container_element">
                    <Button label={'Message 1'} icon='pi pi-envelope'/>
                </div>
                <div className="bot-broadcasting_elements-container_element">
                    <Button label={'Message 2'} icon='pi pi-envelope'/>
                </div>
                <div className="bot-broadcasting_elements-container_element">
                    <Button label={'Добавить'} icon='pi pi-plus'/>
                </div>
            </div>
            <div className="p-col">
                <TextMessage/>
            </div>
        </div>);
    }
}
