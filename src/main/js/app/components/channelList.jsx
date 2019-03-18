import React from "react";
import {Card} from "primereact/card";
import {Button} from "primereact/button";


export default class ChannelList extends React.Component {
    constructor(props) {
        super(props);
    }


    render() {
        const footer1 = (
            <span>
                <Button label={'remove'} className={'p-button-secondary'} icon="pi pi-times"/>
            </span>
        );

        const footer2 = (
            <span>
                <Button label={'Append'} icon="pi pi-angle-up"/>
            </span>
        );
        return (
            <div>
                <div className={"p-grid"}>
                    <Card title={'channel 1'} footer={footer1} className={"p-col channel-list-element"}/>
                    <Card title={'channel 2'} footer={footer1} className={"p-col channel-list-element"}/>
                    <Card title={'channel 3'} footer={footer1} className={"p-col channel-list-element"}/>
                </div>
                <Button label={'Refresh'} icon="pi pi-refresh"/>
                <div className={"p-grid"}>
                    <Card title={'channel 1'} footer={footer2} className={"p-col channel-list-element"}/>
                    <Card title={'channel 2'} footer={footer2} className={"p-col channel-list-element"}/>
                    <Card title={'channel 3'} footer={footer2} className={"p-col channel-list-element"}/>
                </div>
            </div>
        );
    }
}