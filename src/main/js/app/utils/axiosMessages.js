export function serverNotResponse(obj) {
    obj.growl.show({
        severity: 'error',
        summary: obj.props.intl.formatMessage({id: 'app.errormessage.errorsummary'}),
        detail: obj.props.intl.formatMessage({id: 'app.errormessage.servernotresponse'})
    });
}

export function serverErrorResponse(obj, messId) {
    let errorText;
    if (messId in obj.props.intl.messages) {
        errorText = obj.props.intl.formatMessage({id: messId});
    } else {
        errorText = obj.props.intl.formatMessage({id: 'app.errormessage.serverwrongresponse'});
    }
    obj.growl.show({
        severity: 'error',
        summary: obj.props.intl.formatMessage({id: 'app.errormessage.errorsummary'}),
        detail: errorText
    });
}

export function successOperation(obj, messId) {
    let successText;
    if (messId in obj.props.intl.messages) {
        successText = obj.props.intl.formatMessage({id: messId});
    } else {
        successText = obj.props.intl.formatMessage({id: 'app.successmessage.successoperaton'});
    }
    obj.growl.show({
        severity: 'success',
        summary: obj.props.intl.formatMessage({id: 'app.successmessage.successsumary'}),
        detail: successText
    });
}

export function customSuccess(obj, text) {
    obj.growl.show({
        severity: 'success',
        summary: obj.props.intl.formatMessage({id: 'app.successmessage.successsumary'}),
        detail: text
    });
}

export function customError(obj, text) {
    obj.growl.show({
        severity: 'error',
        summary: obj.props.intl.formatMessage({id: 'app.errormessage.errorsummary'}),
        detail: text
    });
}