import {InputText} from "primereact/inputtext";
import {Calendar} from "primereact/calendar";
import {InputTextarea} from "primereact/inputtextarea";
import {Button} from "primereact/button";
import React from "react";

export default class TextMessage extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            title: "",
            firingTime: "",
            erasingTime: "",
            text: "",
        };
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        const message = this.props.message;
        if (message && (!prevProps.message || prevProps.message.id !== message.id)) {
            this.setState({
                title: message.title,
                firingTime: message.firingTime,
                erasingTime: message.erasingTime,
                text: message.text ? message.text : "",
            });
        }
    }

    render() {
        return (
            this.props.message &&
            <div className='text-card'>
                <div className="text-card_detail-element">
                    <InputText placeholder='Title' value={this.state.title}
                               onChange={(e) => this.setState({title: e.value})}/>
                </div>
                <div className="text-card_detail-element">
                    <Calendar showTime={true} showSeconds={true} placeholder={'Posting time'}
                              onChange={(e) => this.setState({firingTime: e.value})}
                              value={this.state.firingTime}/>
                </div>
                <div className="text-card_detail-element">
                    <Calendar showTime={true} showSeconds={true} placeholder={'Erasing time'}
                              onChange={(e) => this.setState({erasingTime: e.value})}
                              value={this.state.erasingTime}/>
                </div>
                <div className="text-card_detail-element">
                    <InputTextarea placeholder={'MessageText'} value={this.state.text} rows={5} cols={60}
                                   onChange={(e) => this.setState({text: e.value})}
                                   autoResize={true}/>
                </div>
                <div className="text-card_button-panel">
                    <Button label='Запланировать'/>
                    <Button label='Удалить'/>
                </div>
            </div>
        );
    }
}