var fiber = require('/modules/sfiber.js');

fiber.app.components.register('c2', function () {

    var handle = function (context,handlers) {
        context.data.c2 = 'c2';
        var log = new Log();
        log.info('Running c2 logic');
        handlers();
    };

    return{
        handle: handle
    }
});