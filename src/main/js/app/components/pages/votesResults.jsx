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
        const {intl} = this.props;
        this.ATTACHMENT_TYPES = {
            file: intl.formatMessage({id: "app.attach.type.file"}),
            image: intl.formatMessage({id: "app.attach.type.image"}),
            video: intl.formatMessage({id: "app.attach.type.video"}),
            audio: intl.formatMessage({id: "app.attach.type.audio"}),
        };

        this.createTabs = this.createTabs.bind(this);
        this.refresh = this.refresh.bind(this);
        this.cellTemplate = this.cellTemplate.bind(this);
    }

    static filterColumn(value, filter) {
        const res = String(value.text).search(new RegExp("[\s\S]*" + filter + "[\s\S]*", "i"));
        return res !== -1;
    }

    cellTemplate(data, column) {
        const cellData = data[column.field];
        if (!cellData) {
            return (<div/>);
        }
        let attachments = [];
        if (Array.isArray(cellData.attachments)) {
            cellData.attachments.forEach((attach) => {
                if (attach.url && this.ATTACHMENT_TYPES[attach.type]) {
                    attachments.push(<div className={"vote-table-cell_attach"}>
                        <a href={attach.url}>{this.ATTACHMENT_TYPES[attach.type]}</a>
                    </div>)
                }
            });
        }

        const {intl} = this.props;
        return (<div className={"vote-table-cell"}>
            <div className={"vote-table-cell_text"}>{cellData.text}</div>
            {attachments.length > 0 && <h5 className={"vote-table-cell_attach-title"}>{intl.formatMessage({id: 'app.attachments'})}:</h5>}
            {attachments}
        </div>);
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
                            userId: {text: vote[VotesResults.SYSTEM_FIELDS[0]], attachments: null},
                            username: {text: vote[VotesResults.SYSTEM_FIELDS[1]], attachments: null},
                            time: {
                                text: dateFormat(new Date(vote[VotesResults.SYSTEM_FIELDS[2]]), this.props.intl.locale === "ru" ? "dd-mm-yyyy H:MM" : "dd-mm-yyyy h:MM TT"),
                                attachments: null
                            }
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
                    <Column field={key} header={key} filter={true} filterFunction={VotesResults.filterColumn}
                            filterMatchMode={"custom"} body={this.cellTemplate}/>);
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
