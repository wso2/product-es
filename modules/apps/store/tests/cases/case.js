describe('.push()-1', function () {
    it('should append a value', function () {
        var carbon = require('carbon');
        var tenantId = carbon.server.tenantId();
        expect(tenantId).to.equal(-1234);
    });

    it('should return the length', function () {
        var arr = [];
        var n = arr.push('foo');
        expect(n).to.equal(1);
        var n = arr.push('bar');
        expect(n).to.equal(2);
    });
});