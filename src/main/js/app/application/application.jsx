import React from 'react';
import {createBrowserHistory} from 'history';
import {Provider} from 'react-redux';
import {applyMiddleware, createStore} from 'redux';
import {withRouter} from 'react-router-dom';
import {ConnectedRouter, routerMiddleware} from 'react-router-redux';
import {composeWithDevTools} from 'redux-devtools-extension';
import IndexLayout from 'app/components/layout';
import {reducers} from 'app/reduser/redusers';
import LocaleProvider from 'app/application/localeProvider'

export default function Application() {
    const WrappedPage = withRouter(IndexLayout);
    const history = createBrowserHistory();

    return (
        <Provider store={createStore(reducers, composeWithDevTools(applyMiddleware(routerMiddleware(history))))}>
            <LocaleProvider>
                <ConnectedRouter history={history}>
                    <WrappedPage/>
                </ConnectedRouter>
            </LocaleProvider>
        </Provider>
    )
};
