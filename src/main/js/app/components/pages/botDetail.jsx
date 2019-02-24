import React from 'react';
import {connect} from 'react-redux';
import {Redirect} from 'react-router-dom'

class BotDetail extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        if (!this.props.isLogin) {
            return (<Redirect to='/'/>);
        }
        return (<div>Bot detail {this.props.match.params.id}</div>);
    }
}

const mapStateToProps = state => ({
    userId: state.userInfo.userId,
    isLogin: state.userInfo.userId != null
});

export default connect(mapStateToProps)(BotDetail);
