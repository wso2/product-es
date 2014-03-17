var router = require('router');

router.app.get('/assets/:type', function (req, res, session) {
    print('Asset listing page ' + req.params.type);
});