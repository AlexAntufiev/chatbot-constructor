export function serverNotResponse(obj) {
    obj.growl.show({
        severity: 'error',
        summary: obj.props.intl.formatMessage({id: 'app.errormessage.errorsummary'}),
        detail: obj.props.intl.formatMessage({id: 'app.errormessage.servernotresponse'})
    });
}

export function serverErrorResponse(obj, errorId) {
    let errorText;
    if (errorId in obj.props.intl.messages) {
        errorText = obj.props.intl.formatMessage({id: errorId});
    } else {
        errorText = obj.props.intl.formatMessage({id: 'app.errormessage.serverwrongresponse'});
    }
    obj.growl.show({
        severity: 'error',
        summary: obj.props.intl.formatMessage({id: 'app.errormessage.errorsummary'}),
        detail: errorText
    });
}

export function successOperation(obj) {
    obj.growl.show({
        severity: 'success',
        summary: obj.props.intl.formatMessage({id: 'app.successmessage.successsumary'}),
        detail: obj.props.intl.formatMessage({id: 'app.successmessage.successoperaton'})
    });
}
