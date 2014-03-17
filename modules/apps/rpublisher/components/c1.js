var fiber = require('fiber');

fiber.app.component('c1', function (context, handlers) {
    var log = new Log('c1');
    log.info('Doing some c1 logic!');
    context.data.c1 = 'c1';
    log.info('Invoking next component');
    handlers();
});


