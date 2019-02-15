const localeRu = {
    firstDayOfWeek: 1,
    today: "Сегодня",
    clear: "Очистить",
    dayNames: ["воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота"],
    dayNamesShort: ["вск", "пн", "вт", "ср", "чт", "пт", "сб"],
    dayNamesMin: ["ВC", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ"],
    monthNames: ["январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"],
    monthNamesShort: ["янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"]
};

const localeEn = {
    firstDayOfWeek: 0,
    today: "Today",
    clear: "Clear",
    dayNames: ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"],
    dayNamesShort: ["sun", "mon", "tue", "wed", "thu", "fri", "sat"],
    dayNamesMin: ["SU", "M", "TU", "W", "TH", "F", "SA"],
    monthNames: ["january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december"],
    monthNamesShort: ["jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"]
};

export default function getCalendar(locale) {
    switch (locale) {
        case "ru":
            return localeRu;
        case "en":
            return localeEn;
        default:
            return null;
    }
}
