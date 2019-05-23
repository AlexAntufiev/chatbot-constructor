import * as React from "react";
import {withRouter} from "react-router";
import {injectIntl} from "react-intl";
import {TabPanel, TabView} from "primereact/tabview";
import * as VotesService from "app/service/votes";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import * as BuilderService from "app/service/builder";
import {Button} from "primereact/button";
import dateFormat from "dateformat";

class VotesResults extends React.Component {

    static get SYSTEM_FIELDS() {
        return ["userId", "username", "time"];
    }

    constructor(props) {
        super(props);

        this.state = {
            votes: {},
            groups: {},
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
            let votes = {};
            res.data.payload.forEach((group) => {
                groups[group.id] = group.title;
                votes[group.id] = [];
            });
            this.setState({groups: groups});
            VotesService.getVotesList(botSchemeId, (res) => {
                res.data.payload.forEach((vote) => {
                    if (votes[vote.groupId]) {
                        let voteObj = {
                            userId: vote[VotesResults.SYSTEM_FIELDS[0]],
                            username: vote[VotesResults.SYSTEM_FIELDS[1]],
                            time: dateFormat(new Date(vote[VotesResults.SYSTEM_FIELDS[2]]), this.props.intl.locale === "ru" ? "dd-mm-yyyy H:MM" : "dd-mm-yyyy h:MM TT")
                        };
                        vote.voteFields.forEach((voteData) => {
                            voteObj[voteData.field] = voteData.value;
                        });
                        votes[vote.groupId].push(voteObj);
                    }
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
            let keys = [];
            this.state.votes[voteId].forEach((ans) => {
                keys = keys.concat(Object.keys(ans).filter(value => keys.indexOf(value) < 0));
            });
            //move system fields to start of columns array
            let i = 0;
            while (i < keys.length) {
                const ind = VotesResults.SYSTEM_FIELDS.indexOf(keys[i]);
                if (ind !== -1) {
                    keys.splice(i, 1);
                } else {
                    i++;
                }
            }
            keys = VotesResults.SYSTEM_FIELDS.concat(keys);

            keys.forEach(key => {
                columns.push(
                    <Column field={key} header={key} filter={true}/>);
            });

            renderedTabs.push(<TabPanel header={this.state.groups[voteId]}>
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
