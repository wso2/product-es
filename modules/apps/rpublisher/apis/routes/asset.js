var router = require('router');


router.app.get('/asset/:type/:id', function (req, res, session) {
    var fiber = require('/modules/fiber.js');
    var data = {};

    fiber.getApp().
        chain('c1').
        chain('c2').
        chain(function (context, handlers) {
            var log=new Log();
            log.info('This is come logic');
        }).
        resolve(data, req, res, session);

    //var data=fiber.getApp().start(req, res, session).chain('c1').chain('c2').chain('c3');

    var data = app.chain('c1', 'c2', 'cn', req, res, ses);

    var data = app.chain(['c1', 'c2', 'cn'], req, res, ses);

    app.chain('c1').chain('c2').chain('c3').chain(function (req, res) {

    });

    app.chain(require('/foo.js')).chain(require('/bar.js'));
    app.chain('foo.js').chain(require('/bar.js'));

    res.render(data);
    //print('EDIT asset page');

});

router.app.get('/asset/:type', function () {

    print('CREATE asset page');

});

res.pipe(createUser).error(createUserError).pipe(sendEmail).pipe(someOtherFun);
