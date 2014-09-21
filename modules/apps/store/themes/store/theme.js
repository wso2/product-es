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
                var output = '';
                //log.info('options: '+stringify(options));
                switch (options.type) {
                    case 'text':
                        output = '<input type="text" class="span12" name="'+options.name.fullName+'" />';
                        break;
                    case 'options':
                        output = '<select id="'+options.name.fullName+'" class="span12 selectpicker " name="'+options.name.fullName+'">';
                        var valueObj=options.values?options.values[0]:{};
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
            Handlebars.registerHelper('authentication',function(options){
                var log=new Log();
                //Determine if security details are present
                var security=options.security;
                var output="";
                var ptr;
                if(!security){
                    log.warn('Unable to locate security details in order to render authentication ui elements');
                    return;
                }
                log.info('Active authentication method: '+security.method);
                //Determine the authentication method
                switch(security.method){
                    case 'sso':
                        log.info('Placing sso related partials');
                        output="{{> sso_auth .}}";
                        break;
                    case 'basic':
                        log.info('Placing basic authentication partials');
                        output="{{> basic_auth .}}";
                        break;
                    default:
                        break;
                }
                ptr=Handlebars.compile(output);
                return new Handlebars.SafeString(ptr(security));
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
    var app=require('rxt').app;

    var appPath=app.resolve(request,path,this.name,this,themeResolver,session);
    if(!appPath){
    	path = asset.resolve(request, path, this.name, this, themeResolver);
    }
    else{
    	path=appPath;
    }

    //log.info('Final path: '+path);
    //path=app.resolve(request,path,this.name,this,themeResolver,session);
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