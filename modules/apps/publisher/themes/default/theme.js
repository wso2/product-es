var cache = false;
var log = new Log();
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
                            publisher = require('/modules/publisher.js');
                        if (asset) {
                            p = publisher.ASSETS_EXT_PATH + asset + '/themes/' + theme.name + '/' + path;
                            if (new File(p).isExists()) {
                                return p;
                            }
                        }
                        return theme.__proto__.resolve.call(theme, path);
                    };
                partials(new File(resolve('partials')));
                return options.fn(this);
            });
            Handlebars.registerHelper('eachField', function(context, options) {
                var ret = '';
                for (var key in context) {
                    context[key].label = context[key].name.label ? context[key].name.label : context[key].name.name;
                    ret += options.fn(context[key]);
                }
                return ret;
            });
            var renderOptionsTextPreview = function(field) {
                var value;
                var values = field.value;
                var output = '';
                for (var index in values) {
                    value = values[index];
                    var delimter = value.indexOf(':')
                    var option = value.substring(0, delimter);
                    var text = value.substring(delimter + 1, value.length);
                    output += '<tr><td>' + option + '</td><td>' + text + '</td></tr>';
                }
                return output;
            };
            var renderDefaultPreview = function(field) {
                var label = field.name.label ? field.name.label : field.name.name;
                return '<td>' + label + '</td><td>' + field.value + '</td>';
            };
            Handlebars.registerHelper('renderFieldPreview', function(field) {
                var out;
                switch (field.type) {
                    case 'option-text':
                        out = renderOptionsTextPreview(field);
                        break;
                    default:
                        out = ''; //renderDefaultPreview(field);
                        break;
                }
                return new Handlebars.SafeString(out);
            });
            Handlebars.registerHelper('renderFieldsPreview', function(table) {
                var heading = (context.subheading) ? context.subheading[0].heading : [];
                var field;
                var out = '';
                for (var index = 0; index < heading.length; index++) {
                    for (var key in table.fields) {
                        field = table.fields[key];
                        out += '<td>' + field.value[index]
                    }
                }
            });
            Handlebars.registerHelper('renderTable', function(context) {
                var unboundedPtr = Handlebars.compile('{{> heading_table .}}');
                var defaultPtr = Handlebars.compile('{{> default_table .}}');
                var out;
                var heading = (context.subheading) ? context.subheading[0].heading : [];
                if (heading.length > 0) {
                    context.subheading = context.subheading[0].heading;
                    out = unboundedPtr(context);
                } else {
                    out = defaultPtr(context);
                }
                return new Handlebars.SafeString(out);
            });
            var getHeadings = function(table) {
                return (table.subheading) ? table.subheading[0].heading : [];
            };
            var getNumOfRows = function(table) {
                for (var key in table.fields) {
                    return table.fields[key].value.length;
                }
                return 0;
            }
            Handlebars.registerHelper('renderUnboundTablePreview', function(table) {
                //Get the number of rows in the table
                var rowCount = getNumOfRows(table);
                var fields = table.fields;
                var out = '';
                for (var index = 0; index < rowCount; index++) {
                    out += '<tr>';
                    for (var key in fields) {
                        out += '<td>' + fields[key].value[index] + '</td>';
                    }
                    out += '</tr>';
                }
                return new Handlebars.SafeString(out);
            });
            Handlebars.registerHelper('renderTablePreview', function(table) {
                var headingPtr = Handlebars.compile('{{> heading_table .}}');
                var defaultPtr = Handlebars.compile('{{> default_table .}}');
                var unboundPtr = Handlebars.compile('{{> unbound_table .}}');
                var headings = getHeadings(table);
                //Check if the table is unbounded
                if ((table.maxoccurs) && (table.maxoccurs == 'unbounded')) {
                    if (headings.length > 0) {
                        table.subheading = table.subheading[0].heading;
                    }
                    return new Handlebars.SafeString(unboundPtr(table));
                }
                //Check if the table has headings
                if (headings.length > 0) {
                    table.subheading = table.subheading[0].heading;
                    return new Handlebars.SafeString(headingPtr(table));
                }
                //Check if the table is a normal table
                return new Handlebars.SafeString(defaultPtr(table));
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
            var publisher = require('/modules/publisher.js'),
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
    /*var p,
        publisher = require('/modules/publisher.js'),
        asset = publisher.currentAsset();
    if (asset) {
        p = publisher.ASSETS_EXT_PATH + asset + '/themes/' + this.name + '/' + path;
        if (new File(p).isExists()) {
            return p;
        }
    }
    var actualPath=this.__proto__.resolve.call(this, path);
    log.info('Actual path: '+actualPath);*/
    return path;
};