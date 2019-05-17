export default {
    //Home page
    'app.page.home.text': 'Домашняя страница',

    //Bot list page
    'app.bot.edit': 'Редактировать',
    'app.bot.remove': 'Удалить',

    //Bot detail
    'app.bot.detail.select.channels': 'Выберите каналы',

    //Menu
    'app.menu.home': 'Домой',
    'app.menu.botlist': 'Список ботов',
    'app.menu.signin': 'Вход',
    'app.menu.signup': 'Регистрация',
    'app.menu.logout': 'Выход',
    'app.menu.settings': 'Настройки',
    'app.menu.constructor': 'Конструктор',
    'app.menu.statistic': 'Статистика',
    'app.menu.broadcasting': 'Рассылка',

    //Dialogs
    'app.dialog.name': 'Название',
    'app.dialog.picture': 'Изображение',
    'app.dialog.description': 'Описание',
    'app.dialog.token': 'Токен',
    'app.dialog.create': 'Создать',
    'app.dialog.close': 'Закрыть',
    'app.dialog.save': 'Сохранить',
    'app.dialog.cancel': 'Отменить',
    'app.dialog.login': 'Вход',
    'app.dialog.username': 'Логин',
    'app.dialog.password': 'Пароль',
    'app.dialog.registration': 'Регистрация',
    'app.dialog.confirmpassword': 'Повторите пароль',
    'app.dialog.password.weak': 'Слабый',
    'app.dialog.password.medium': 'Нормальный',
    'app.dialog.password.strong': 'Сильный',
    'app.dialog.password.enter': 'Введите пароль',
    'app.dialog.checksure': 'Вы уверены?',
    'app.dialog.connect': 'Подключить',
    'app.dialog.disconnect': 'Отключить',
    'app.dialog.append': 'Добавить',
    'app.dialog.refresh': 'Обновить',
    'app.dialog.attach': 'Прикрепить',
    'app.dialog.remove.button': 'Удалить кнопку',
    'app.dialog.remove.message': 'Удалить сообщение',
    'app.dialog.add.message': 'Добавить сообщение',

    //Error messages
    'app.errormessage.errorsummary': 'Сообщение об ошибке',
    'app.errormessage.passwordnotmatch': 'Пароли не совпадают',
    'app.errormessage.fillallfields': 'Заполните все поля',
    'app.errormessage.fillname': 'Заполните имя',
    'app.errormessage.servernotresponse': 'Сервер недоступен',
    'app.errormessage.serverwrongresponse': 'Неверный ответ от сервера',
    'errors.service.error': "Ошибка сервиса",
    'errors.tam.service': 'Ошибка сервиса ТамТам',
    'errors.tam.bot.token.incorrect': 'Неправильный токен',
    'errors.tam.bot.token.empty': 'Пустой токен',
    'errors.tam.bot.subscribed.already': 'Бот уже подключен',
    'errors.tam.bot.unsubscribed.already': 'Бот уже отключен',
    'errors.tam.bot.not.subscribed': "Бот не подключен",
    'errors.tam.bot.connected.to.other.bot.scheme': 'Бот уже подключен к другой схеме',
    'errors.chatChannel.selected.empty': 'Id канала не указан',
    'errors.chatChannel.permission': 'Бот не является администратором канала',
    'errors.chatChannel.does.not.exist': 'Канал не существует',
    'errors.not.chatChannel': 'Это не канал',
    'errors.broadcast.message.does.not.exist': 'Сообщения не существует',
    'errors.broadcast.message.firing.time.is.in.past': 'Время для отправки прошло',
    'errors.broadcast.message.firing.time.is.null': 'Время для отправки не установлено',
    'errors.broadcast.message.title.is.empty': 'Пустое название сообщения',
    'errors.broadcast.message.erasing.time.is.before.then.firing.time': 'Время отпарвки позже времени удаления',
    'errors.broadcast.message.illegal.state': 'Невозможно изменить сообщение',
    'errors.broadcast.message.send.error': 'Во время отправки произошла ошибка',
    'errors.broadcast.message.erase.error': 'Во время удаления произошла ошибка',
    'errors.broadcast.message.send.already.discarded': 'Отправка уже отменена',
    'errors.broadcast.message.erase.already.discarded': 'Удаление уже отменено',
    'errors.broadcast.message.firing.time.is.malformed': 'Ошибка во времени отправки',
    'errors.broadcast.message.erasing.time.is.malformed': 'Ошибка во времени удаления',
    'errors.broadcast.message.erasing.time.is.in.the.past': 'Время удаления в прошлом',
    'errors.broadcast.message.text.is.empty': 'Пустой текст сообщения',
    'error.broadcast.message.need.fill.text.and.posttime': 'Заполните текст и время постинга',
    'errors.broadcast.message.has.too.much.attachments': 'Слишком много вложений',
    'errors.attachment.type.is.empty': 'Укажите тип приложения',
    'errors.attachment.type.is.illegal': 'Тип приложения не поддерживается',
    'errors.attachment.upload.service.error': 'Ошибка сервиса загрузки',
    'errors.attachment.does.not.exist': 'Приложение не существует',
    'errors.attachment.token.is.not.valid': 'Неверный идентификатор приложения',
    'errors.bot.scheme.invalid.validator': 'Передан неверный валидатор',
    'errors.bot.scheme.builder.buttons.empty.fields': 'Кнопки не должны иметь пустые поля',
    'errors.bot.scheme.builder.buttons.id.not.exist': 'Id группы кнопок не подходит',
    'errors.bot.scheme.builder.buttons.group.is.empty': 'Группа кнопок не может быть пустой',
    'errors.bot.scheme.builder.buttons.group.intent.malformed': 'Неверный цвет кнопки',
    'errors.bot.scheme.builder.component.id.is.null': 'Пустой id компонента',
    'errors.bot.scheme.builder.component.duplication': 'Повторяющийся id компонента',
    'errors.bot.scheme.builder.component.is.absent': 'Отсутствует компонент с таким id',
    'errors.bot.scheme.builder.component.graph.is.cyclic': 'Зацикленная схема бота',
    'errors.bot.scheme.builder.component.text.is.empty': 'Текст не должен быть пустым',
    'errors.bot.scheme.builder.component.type.is.null': 'Пустой тип компонента',
    'errors.bot.scheme.builder.component.type.is.illegal': 'Неверный тип компонента',
    'errors.bot.scheme.component.group.has.empty.title': 'Пустое название группы',
    'errors.bot.scheme.component.group.is.not.found': 'Группа не найдена',
    'errors.bot.scheme.component.group.type.is.illegal': 'Неверный тип группы',
    'errors.bot.scheme.builder.component.action.is.not.found': 'Действие не найдено',
    'errors.bot.scheme.builder.component.action.has.illegal.type': 'Действие имеет недопустимый тип',
    'errors.bot.scheme.builder.component.action.does.not.belong.to.component': 'Действие не относится к компоненту',

    //Success messages
    'app.successmessage.successsumary': 'Успешно',
    'app.successmessage.successoperaton': 'Успешная операция',
    'success.tam.bot.name.changed': 'Имя изменено',
    'success.tam.bot.subscribed': 'Бот подключен',
    'success.tam.bot.unsubscribed': 'Бот отключен',

    'app.broadcastmessage.send.wait': 'Ждет отправки',
    'app.broadcastmessage.sent': 'Отправлено',
    'app.broadcastmessage.processing': 'Обработка',
    'app.broadcastmessage.sent.erased': 'Отправлено и удалено',
    'app.broadcastmessage.postingtime': 'Время постинга',
    'app.broadcastmessage.erasingtime': 'Время удаления',
    'app.broadcastmessage.text': 'Текст сообщения',
    'app.broadcastmessage.saved': 'Сообщение сохранено',

    'template.message.sent': 'Сообщение :name отправлено',
    'template.message.erase': 'Сообщение :name удалено',
    'template.message.error': 'Произошла ошибка в сообщении :name',

    //Common
    'app.common.sigin.message': 'Необходима авторизация',

    'app.constructor.intent.positive': 'зеленый',
    'app.constructor.intent.negative': 'красный',
    'app.constructor.intent.default': 'серый',
    'app.constructor.intent': 'Цвет',
    'app.constructor.next.component': 'Следующий компонент',
    'app.constructor.component.buttongroup': 'Группа кнопок',
    'app.constructor.component.userinput': 'Ввод пользователя',
    'app.constructor.component.button': 'Кнопка',
    'app.constructor.message.text': 'Текст сообщения',
    'app.constructor.scheme.saved': 'Схема сохранена',
    'app.constructor.error.fill.text.template': 'Текст не может быть пустым ":title"',
    'app.constructor.scheme.add.group': 'Добавить группу',
    'app.constructor.error.empty.buttons.template': 'Группа кнопок не может быть без кнопок ":title"',
    'app.constructor.error.empty.title': 'Пустое название'
};