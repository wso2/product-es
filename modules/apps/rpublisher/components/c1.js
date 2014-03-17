var fiber = require('/modules/sfiber.js');

fiber.app.components.register('c1', function () {

    var handle = function (context, handlers) {
        context.data.c1 = 'c1';
        var log = new Log();
        log.info('Running c1 logic');
        handlers();
    };

    return{
        handle: handle
    }
});


