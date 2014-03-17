var router = require('router');
var log = new Log('asset-controller');

router.app.get('/asset/:type/:id', function (req, res, session) {
    print('Asset details page');
});

router.app.get('/asset/:type', function (req, res, session) {
    print('Create asset page');
});