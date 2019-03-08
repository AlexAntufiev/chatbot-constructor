import React from 'react';
import {connect} from "react-redux";
import {SelectButton} from 'primereact/selectbutton';
import setLocale from 'app/actions/locale'
import {AUTHORIZATION} from "app/constants/cookies";

class Locale extends React.Component {
    constructor(props) {
        super(props);
        this.onChange = this.onChange.bind(this);
        this.state = {locale: props.locale};
    }

    onChange(e) {
        if (this.state.locale == e.value || e.value == null) {
            e.preventDefault();
        } else {
            this.props.setLocale(e.value);
            this.setState({locale: e.value});
        }
    }

    render() {
        const options = [
            {label: 'ru', value: 'ru'},
            {label: 'en', value: 'en'}
        ];

        return (
            <SelectButton value={this.state.locale} options={options} onChange={this.onChange}/>
        );
    }
}

const mapStateToProps = state => ({
    locale: state.locale.locale
});

const mapDispatchToProps = dispatch => ({
    setLocale: locale => dispatch(setLocale(locale))
});

export default connect(mapStateToProps, mapDispatchToProps)(Locale);
