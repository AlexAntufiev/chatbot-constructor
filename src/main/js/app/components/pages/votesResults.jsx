import * as React from "react";
import {withRouter} from "react-router";
import {injectIntl} from "react-intl";
import {TabPanel, TabView} from "primereact/tabview";
import * as VotesService from "app/service/votes";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import * as BuilderService from "../../service/builder";
import {Button} from "primereact/button";

class VotesResults extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            votes: {},
            groups:{},
            ajaxRefreshProcess: false
        };

        this.createTabs = this.createTabs.bind(this);
        this.refresh = this.refresh.bind(this);
    }

    componentDidMount() {
        this.refresh();
    }

    refresh() {
        const botSchemeId = Number(this.props.match.params.id);
        this.setState({ajaxRefreshProcess: true});
        BuilderService.getGroups(botSchemeId, (res) => {
            let groups = {};
            res.data.payload.forEach((group) => {
                groups[group.id] = group.title;
            });
            this.setState({groups: groups});
            VotesService.getVotesList(botSchemeId, (res) => {
                let votes = {};
                res.data.payload.forEach((vote) => {
                    let voteObj = {userId: vote.userId};
                    vote.dataAsList.forEach((voteData) => {
                        voteObj[voteData.field] = voteData.value;
                    });
                    votes[vote.groupId].push(votes);
                });
                this.setState({
                    votes: votes,
                    ajaxRefreshProcess: false
                });
            }, () => this.setState({ajaxRefreshProcess: false}), this);
        }, () => this.setState({ajaxRefreshProcess: false}), this);
    }

    createTabs() {
        let renderedTabs = [];

        for (let voteId in this.state.votes) {
            let columns = [];
            if (this.state.votes[voteId].length === 0) continue;
            const keys = Object.keys(this.state.votes[voteId][0]);

            keys.forEach(key => {
                columns.push(
                    <Column field={key} header={key} filter={true} />);
            });

            renderedTabs.push(<TabPanel header={this.state.groups[voteId].title}>
                <DataTable value={this.state.votes[voteId]}>
                    {columns}
                </DataTable>
            </TabPanel>)
        }
        return renderedTabs;
    }

    render() {
        const {intl} = this.props;
        const tabs = this.createTabs();
        return (<div>
            <Button label={intl.formatMessage({id: 'app.dialog.refresh'})}
                    className={'vote-refresh-button'}
                    icon={"pi pi-refresh" + (this.state.ajaxRefreshProcess ? " pi-spin" : "")}
                    disabled={this.state.ajaxRefreshProcess}
                    onClick={this.refresh}/>
            <TabView>
                {tabs}
            </TabView>
        </div>);
    }

}

export default withRouter(injectIntl(VotesResults, {withRef: true}));
