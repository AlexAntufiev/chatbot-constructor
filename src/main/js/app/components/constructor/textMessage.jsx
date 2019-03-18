import {InputText} from "primereact/inputtext";
import {Calendar} from "primereact/calendar";
import {InputTextarea} from "primereact/inputtextarea";
import {Button} from "primereact/button";
import React from "react";

export default class TextMessage extends React.Component{
    constructor(props) {
        super(props);
    }

    render() {

        return (
        <div className='text-card'>
            <div className="text-card_detail-element">
                <InputText placeholder='name'/>
            </div>
            <div className="text-card_detail-element">
                <Calendar showTime={true} showSeconds={true} placeholder={'Post date'}/>
            </div>
            <div className="text-card_detail-element">
                <InputTextarea placeholder={'MessageText'} rows={5} cols={60} autoResize={true}/>
            </div>
            <div className="text-card_button-panel">
                <Button label='Запланировать'/>
                <Button label='Удалить'/>
            </div>
        </div>
        );
    }
}