export default function makeUrl(url, params) {
    for (let k in params) {
        url = url.replace(':' + String(k), String(params[k]));
    }
    return url;
}
