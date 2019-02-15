import React from 'react';
import {connect} from 'react-redux';
import {FormattedMessage} from 'react-intl';

export function HomeLayout(props) {

    return (
        <div><FormattedMessage id='app.page.home.text'/></div>
    );
}

export default connect()(HomeLayout);
