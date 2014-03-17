var fiber = require('fiber');

fiber.app.component('c2', function (context,handlers) {
    var log=new Log('c2');
    log.info('Doing some c2 logic!');
    context.data.c2='c2';
    handlers();
});