var router = require('router');


router.app.get('/asset/:type/:id', function (req, res, session) {
    var fiber = require('/modules/fiber.js');
    var data = {};

    fiber.getApp().
        chain('c1').
        chain('c2').
        chain('c3,c4').
        chain(function (context, handlers) {
            var log = new Log();
            log.info('This is some logic');
        }).
        finally(function(){
            log.info('Final logic!');
        }).
        resolve(data, req, res, session);

    res.render(data);
    //print('EDIT asset page');
});

router.app.get('/asset/:type', function () {

    print('CREATE asset page');

});


