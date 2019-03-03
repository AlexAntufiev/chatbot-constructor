export function serverNotResponse(obj) {
    obj.growl.show({
        severity: 'error',
        summary: obj.props.intl.formatMessage({id: 'app.errormessage.errorsummary'}),
        detail: obj.props.intl.formatMessage({id: 'app.errormessage.servernotresponse'})
    });
}

export function serverErrorResponse(obj) {
    obj.growl.show({
        severity: 'error',
        summary: obj.props.intl.formatMessage({id: 'app.errormessage.errorsummary'}),
        detail: obj.props.intl.formatMessage({id: 'app.errormessage.serverwrongresponse'})
    });
}

export function successOperation(obj) {
    obj.growl.show({
        severity: 'success',
        summary: obj.props.intl.formatMessage({id: 'app.successmessage.successsumary'}),
        detail: obj.props.intl.formatMessage({id: 'app.successmessage.successoperaton'})
    });
}