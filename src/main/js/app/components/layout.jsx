import React, {Component} from 'react';
import {connect} from 'react-redux';
import * as routers from 'app/constants/routes'
import ApplicationRouter from 'app/router/routerSwitch';
import AuthPanel from 'app/components/authPanel';
import Locale from 'app/components/locale';
import {Menubar} from 'primereact/menubar';
import {injectIntl} from 'react-intl';

class IndexLayout extends Component {
    constructor(props) {
        super(props);

        const {intl} = this.props;
        this.state = {
            items: [
                {
                    label: intl.formatMessage({id: 'app.menu.home'}),
                    command: (event) => {
                        this.props.history.push(routers.index())
                    }
                }
            ]
        };

        if (this.props.isLogin) {
            this.state.items.push(
                {
                    label: intl.formatMessage({id: 'app.menu.botlist'}),
                    command: (event) => {
                        this.props.history.push(routers.botList())
                    }
                });
        }
    }

    render() {
        const {intl} = this.props;

        return (
            <div>
                <div>
                    <Menubar model={this.state.items}>
                        <div style={{display: 'flex'}}>
                            <AuthPanel/>
                            <Locale/>
                        </div>

                    </Menubar>
                </div>
                <ApplicationRouter/>
            </div>
        );
    }
}

function mapStateToProps(state) {
    return {
        userId: state.userInfo.userId,
        isLogin: state.userInfo.userId != null
    };
}

export default connect(mapStateToProps)(injectIntl(IndexLayout));
