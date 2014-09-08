var cache = false;
var engine = caramel.engine('handlebars', (function() {
    return {
        partials: function(Handlebars) {
            var theme = caramel.theme();
            var partials = function(file) {
                (function register(prefix, file) {
                    var i, length, name, files;
                    if (file.isDirectory()) {
                        files = file.listFiles();
                        length = files.length;
                        for (i = 0; i < length; i++) {
                            file = files[i];
                            register(prefix ? prefix + '.' + file.getName() : file.getName(), file);
                        }
                    } else {
                        name = file.getName();
                        if (name.substring(name.length - 4) !== '.hbs') {
                            return;
                        }
                        file.open('r');
                        Handlebars.registerPartial(prefix.substring(0, prefix.length - 4), file.readAll());
                        file.close();
                    }
                })('', file);
            };
            //TODO : we don't need to register all partials in the themes dir.
            //Rather register only not overridden partials
            partials(new File(theme.__proto__.resolve.call(theme, 'partials')));
            partials(new File(theme.resolve('partials')));
            Handlebars.registerHelper('dyn', function(options) {
                var asset = options.hash.asset,
                    resolve = function(path) {
                        var p,
                            store = require('/modules/store.js');
                        if (asset) {
                            p = store.ASSETS_EXT_PATH + asset + '/themes/' + theme.name + '/' + path;
                            if (new File(p).isExists()) {
                                return p;
                            }
                        }
                        return theme.__proto__.resolve.call(theme, path);
                    };
                partials(new File(resolve('partials')));
                return options.fn(this);
            });
            Handlebars.registerHelper('renderSearchField', function(options) {
                var log = new Log();
                log.info(options);
                var output = '';
                switch (options.type) {
                    case 'text':
                        output = '<input type="text" class="span12" />';
                        break;
                    case 'options':
                        output = '<select id="'+options.name.fullName+'" class="span12 selectpicker " name="'+options.name.fullName+'">';
                        var valueObj=options.values?options.values[0]:{};
                        log.info('Values: '+stringify(valueObj));
                        var values=valueObj.value?valueObj.value:[];
                        for(var index in values){
                            output+='<option>'+values[index].value+'</option>';
                        }
                        output+='</select>';
                        break;
                    default:
                        log.warn('Unable to render search field: ' + options.name.name + ' as the type is not supported');
                        break;
                }
                return new Handlebars.SafeString(output);
            });
        },
        render: function(data, meta) {
            if (request.getParameter('debug') == '1') {
                response.addHeader("Content-Type", "application/json");
                print(stringify(data));
            } else {
                this.__proto__.render.call(this, data, meta);
            }
        },
        globals: function(data, meta) {
            var store = require('/modules/store.js'),
                user = require('store').server.current(meta.session);
            return 'var store = ' + stringify({
                user: user ? user.username : null
            });
        }
    };
}()));
var resolve = function(path) {
    var themeResolver = this.__proto__.resolve;
    var asset = require('rxt').asset;
    path = asset.resolve(request, path, this.name, this, themeResolver);
    // var p,
    //     store = require('/modules/store.js'),
    //     asset = store.currentAsset();
    // if (asset) {
    //     p = store.ASSETS_EXT_PATH + asset + '/themes/' + this.name + '/' + path;
    //     if (new File(p).isExists()) {
    //         return p;
    //     }
    // }
    // return this.__proto__.resolve.call(this, path);
    return path;
};