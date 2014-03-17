var router = require('router');

var log=new Log();
log.info('Registering asset');

router.app.get('/asset/:type/:id', function (req, res, session) {
    var fiber = require('/modules/sfiber.js');
    var data = {};

    fiber.component.
        chain('c1').
        chain('c2').
        chain('c3,c4').
        chain(function (context, handlers) {
            log.info('This is some logic');
        }).
        finally(function(){
            log.info('Finishing component logic');
        }).
        resolve(data, req, res, session);

    res.render(data);
    //print('EDIT asset page');
});

router.app.get('/asset/:type', function () {

    print('CREATE asset page');

});


