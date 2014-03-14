var fiber = require('/modules/fiber.js');

fiber.register('c1', function () {

    var handle = function (context, handlers) {
        context._c1 = 'c1';
        var log = new Log();
        log.info('Running c1 logic');
        handlers();
    };

    return{
        handle: handle
    }
});


