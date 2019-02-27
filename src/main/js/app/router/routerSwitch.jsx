import React from 'react';
import {Route, Switch} from 'react-router';
import * as routes from 'app/constants/routes';
import HomePage from 'app/components/pages/home';
import BotList from 'app/components/pages/botList';
import BotDetail from 'app/components/pages/botDetail';

export default () => (
    <Switch>
        <Route exact path={routes.index()} component={HomePage}/>
        <Route exact path={routes.botList() + ":id/"} component={BotDetail}/>
        <Route exact path={routes.botList()} component={BotList}/>
    </Switch>
);
