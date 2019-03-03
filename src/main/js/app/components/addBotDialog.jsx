import React from 'react';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {InputText} from 'primereact/inputtext';
import {Growl} from 'primereact/growl';
import {BaseDialog} from 'app/components/baseDialog';
import {FileUpload} from 'primereact/fileupload';
import {InputTextarea} from 'primereact/inputtextarea';
import {FormattedMessage, injectIntl} from 'react-intl';

class AddBotDialog extends BaseDialog {
    constructor(props) {
        super(props);

        this.state = {
            name: '',
            description: '',
        };
        this.onCreateBot = this.onCreateBot.bind(this);
    }

    onCreateBot() {
        const {intl} = this.props;

        if (this.state.name.trim() == '') {
            this.growl.show({
                severity: 'error',
                summary: intl.formatMessage({id: 'app.errormessage.errorsummary'}),
                detail: intl.formatMessage({id: 'app.errormessage.fillname'})
            });
        }
        //TODO: create bot request
    }

    render() {
        const {intl} = this.props;
        const footer = (
            <div>
                <Button label={intl.formatMessage({id: 'app.dialog.create'})} icon="pi pi-check"
                        onClick={this.onCreateBot}/>
                <Button label={intl.formatMessage({id: 'app.dialog.close'})} icon="pi pi-times" onClick={this.onHide}
                        className="p-button-secondary"/>
            </div>
        );
        return (
            <div>
                <Growl ref={(el) => this.growl = el}/>
                <Dialog closable={false} footer={footer} visible={this.state.visible}
                        style={{width: '300px'}} modal={true} onHide={this.onHide}>
                    <span className="p-float-label" style={{marginTop: '20px'}}>
                        <InputText id="add-bot-name" value={this.state.name}
                                   onChange={(e) => this.setState({name: e.target.value})}
                                   style={{overflow: 'hidden'}}/>
                        <label htmlFor="add-bot-name"><FormattedMessage id='app.dialog.name'/></label>
                    </span>

                    <div style={{marginTop: '20px'}}>
                        <FileUpload chooseLabel={intl.formatMessage({id: 'app.dialog.picture'})} mode="basic"
                                    name="img[]" url="./upload.php" accept="image/*" maxFileSize={1000000}/>
                    </div>

                    <span className="p-float-label" style={{marginTop: '20px'}}>
                        <InputTextarea placeholder={intl.formatMessage({id: 'app.dialog.description'})} rows={5}
                                       cols={30} value={this.state.description}
                                       onChange={(e) => this.setState({description: e.target.value})}
                                       autoResize={true}/>
                    </span>
                </Dialog>
            </div>
        );
    }
}

export default injectIntl(AddBotDialog, {withRef: true});
