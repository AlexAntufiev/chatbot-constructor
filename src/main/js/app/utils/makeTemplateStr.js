export default function makeTemplateStr(str, params) {
    for (let k in params) {
        str = str.replace(':' + String(k), String(params[k]));
    }
    return str;
}
