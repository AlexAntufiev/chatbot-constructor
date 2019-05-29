import React, {Component} from 'react';

export class BaseDialog extends Component {
    constructor(props) {
        super(props);
        this.onShow = this.onShow.bind(this);
        this.onHide = this.onHide.bind(this);
    }

    onShow() {
        this.setState({visible: true});
    }

    onHide() {
        this.setState({visible: false});
    }
}
