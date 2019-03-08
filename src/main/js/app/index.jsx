import 'primereact/resources/themes/nova-light/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import 'primeflex/primeflex.css';
import 'main.scss'

import React from 'react';
import ReactDOM from 'react-dom';
import Application from 'app/application/application';

const {document} = window;

function render() {
    ReactDOM.render(<Application/>, window.document.getElementById('root'));
}

document.addEventListener('DOMContentLoaded', render);