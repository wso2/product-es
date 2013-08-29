var ASSETS_EXT_PATH = '/assets/';

var ASSET_MANAGERS = 'asset.managers';

var STORE_TENANT = 'store.tenant';

var STORE_CONFIG_PATH = '/_system/config/store/configs/';

var TAGS_QUERY_PATH = '/_system/config/repository/components/org.wso2.carbon.registry/queries/allTags';

//TODO: read from tenant config
var ASSETS_PAGE_SIZE = ( function() {
	return require('/store.js').config().assetsPageSize;
}());

//TODO: read from tenant config
var COMMENTS_PAGE_SIZE = ( function() {
	return require('/store.js').config().commentsPageSize;
}());

var log = new Log();

var tags, init, assets, asset, assetLinks, tagged, popularAssets, recentAssets, assetTypes, currentAsset, isuserasset,
    cache, cached, invalidate, comments, comment, assetsPaging, commentsPaging, rate, rating, assetCount, tenant,
    userAssets, registry, commentCount, search, addAsset, updateAsset, removeAsset, assetManager, configs;

(function() {

    assetManager = function (tenantId, type, reg) {
        var manager, assetManagers, user,
            path = ASSETS_EXT_PATH + type + '/asset.js',
            azzet = new File(path).isExists() ? require(path) : require('/modules/asset.js');
        if (reg) {
            return new azzet.Manager(reg, type);
        }
        user = require('/modules/user.js');
        if (user.current()) {
            assetManagers = session.get(ASSET_MANAGERS);
            if (!assetManagers) {
                assetManagers = {};
                session.put(ASSET_MANAGERS, assetManagers);
            }
            manager = assetManagers[type];
            if (manager) {
                return manager;
            }
            return (assetManagers[type] = new azzet.Manager(user.userRegistry(), type));
        }
        return server.configs(tenantId)[ASSET_MANAGERS][type];
    };

	var merge = function(def, options) {
		if(options) {
			for(var op in def) {
				if(def.hasOwnProperty(op)) {
					def[op] = options[op] || def[op];
				}
			}
		}
		return def;
	};

	init = function(options) {
		//addRxtConfigs(tenantId);
        var event = require('/modules/event.js');

        event.on('tenantLoad', function (tenantId) {
            var user = require('/modules/user.js'),
                server = require('/modules/server.js');
            server.configs(tenantId)[user.USER_OPTIONS] = configs(tenantId);
        });

        event.on('tenantCreate', function (tenantId) {
            var carbon = require('carbon'),
                config = require('/store-tenant.json'),
                server = require('/modules/server.js'),
                system = server.systemRegistry(tenantId),
                um = server.userManager(tenantId),
                GovernanceConstants = org.wso2.carbon.governance.api.util.GovernanceConstants;
            system.put(STORE_CONFIG_PATH + 'store.json', {
                content: JSON.stringify(config),
                mediaType: 'application/json'
            });
            system.put(TAGS_QUERY_PATH, {
                content: 'SELECT RT.REG_TAG_ID FROM REG_RESOURCE_TAG RT ORDER BY RT.REG_TAG_ID',
                mediaType: 'application/vnd.sql.query',
                properties: {
                    resultType: 'Tags'
                }
            });
            um.authorizeRole(carbon.user.anonRole, GovernanceConstants.RXT_CONFIGS_PATH, carbon.registry.actions.GET);
        });

        event.on('login', function (tenantId, user) {
            var assetManagers,
                server = require('/modules/server.js'),
                config = server.configs(tenantId);
            if (!config[ASSET_MANAGERS]) {
                assetManagers = {};
                assetTypes(tenantId).forEach(function (type) {
                    var path = ASSETS_EXT_PATH + type + '/asset.js',
                        azzet = new File(path).isExists() ? require(path) : require('/modules/asset.js');
                    assetManagers[type] = new azzet.Manager(server.anonRegistry(tenantId), type);
                });
                config[ASSET_MANAGERS] = assetManagers;
            }
        });
	};

    /**
     * This is just a util method. You need to validate the tenant before you use. So, USE WITH CARE.
     * @param request
     */
    tenant = function (request) {
        var carbon,
            domain = request.getParameter('domain'),
            user = require('/module/user.js').current();
        if (user) {
            return {
                tenantId: user.tenantId,
                domain: user.domain
            };
        }
        carbon = require('carbon');
        return {
            tenantId: carbon.server.tenantId({
                domain: domain
            }),
            domain: domain
        };
    };

    configs = function (tenantId) {
        var server = require('/modules/server.js'),
            registry = server.systemRegistry(tenantId);
        return JSON.parse(registry.content(STORE_CONFIG_PATH + 'store.json'));
    };

   /* addRxtConfigs = function (tenantId) {
        var CommonUtil = Packages.org.wso2.carbon.governance.registry.extensions.utils.CommonUtil,
            GovernanceConstants = org.wso2.carbon.governance.api.util.GovernanceConstants,
            carbon = require('carbon'),
            server = require('/modules/server.js'),
            reg = server.systemRegistry(tenantId),
            um = server.userManager(tenantId);
        CommonUtil.addRxtConfigs(reg.registry.getChrootedRegistry("/_system/governance"), tenantId);
        um.authorizeRole(carbon.user.anonRole, GovernanceConstants.RXT_CONFIGS_PATH, carbon.registry.actions.GET);
    };
*/

    userAssets = function(user) {
		var items = [],
            user = require('/modules/user.js'),
            registry = user.userRegistry(user.tenantId),
            path = user.userSpace(user.username) + '/userAssets',
            assets = registry.get(path),
            assetz = {};
		if(!assets || assets.length <= 0) {
			return assetz;
		}
        assets.forEach(function (type) {
            var obj = registry.get(path + '/' + type);
            obj.forEach(function(id) {
                    items.push(asset(type, id))
            });
            assetz[type] = items;
        });
		return assetz;
	};

	/**
	 * Retrieves either current user's registry instance or default instance.
	 * @return {String}
	 */
	/*registry = function(tenantId) {
        var user = require('/modules/user.js');
		return user.userRegistry() || require('/modules/server.js').anonRegistry(user.tenantId);
	};
*/
	/**
	 * Returns all tags
	 * @param tenantId Tenant Id
	 * @param type Asset type
	 */
	tags = function(tenantId, type) {
		var tag, tags, assetType, i, length, count,
            tagz = [],
            tz = {};
		tags = registry().query(TAGS_QUERY_PATH);
		length = tags.length;
		if(type == undefined) {
			for( i = 0; i < length; i++) {
				tag = tags[i].split(';')[1].split(':')[1];
				count = tz[tag];
				count = count ? count + 1 : 1;
				tz[tag] = count;
			}
		} else {
			for( i = 0; i < length; i++) {
				assetType = tags[i].split(';')[0].split('/')[3];
				if(assetType != undefined) {
					if(assetType.contains(type)) {
                        tag = tags[i].split(';')[1].split(':')[1];
						count = tz[tag];
						count = count ? count + 1 : 1;
						tz[tag] = count;
					}
				}
			}
		}
        for(tag in tz) {
            if(tz.hasOwnProperty(tag)) {
                var result = assetManager(type).checkTagAssets({tag: tag });
                if (result.length > 0) {
                    tagz.push({
                        name: String(tag),
                        count: tz[tag]
                    });
                }
            }
        }
		return tagz;
	};

	comments = function(tenantId, aid, paging) {
        var registry = require('/modules/user.js').userRegistry() || require('/modules/server.js').anonRegistry(tenantId);
		return registry.comments(aid, paging);
	};

	commentCount = function(tenantId, aid) {
        var registry = require('/modules/user.js').userRegistry() || require('/modules/server.js').anonRegistry(tenantId);
		return registry.commentCount(aid);
	};

    comment = function (tenantId, aid, comment) {
        var registry = require('/modules/user.js').userRegistry() || require('/modules/server.js').anonRegistry(tenantId);
        return registry.comment(aid, comment);
    };

    rating = function (tenantId, aid) {
        var username, registry,
            carbon = require('carbon'),
            user = require('/modules/user.js'),
            usr = user.current();
        if (usr) {
            registry = user.userRegistry();
            username = usr.username;
        } else {
            registry = require('/modules/server.js').anonRegistry(tenantId);
            username = carbon.user.anonUser;
        }
        return registry.rating(aid, username);
    };

	rate = function(tenantId, aid, rating) {
        var registry = require('/modules/user.js').userRegistry() || require('/modules/server.js').anonRegistry(tenantId);
		return registry.rate(aid, rating);
	};

	/**
	 * Returns all assets for the current user
	 * @param tenantId tenant id
	 * @param type Asset type
	 * @param paging
	 */
    assets = function (tenantId, type, paging) {
        var i,
            assetz = assetManager(tenantId, type).list(paging);
        for (i = 0; i < assetz.length; i++) {
            assetz[i].indashboard = isuserasset(assetz[i].id, type);
        }
        return assetz;
    };

    tagged = function (tenantId, type, tag, paging) {
        var i,
            options = {
                tag: tag,
                attributes: {
                    'overview_status': /^(published)$/i
                }
            },
            assets = assetManager(tenantId, type).search(options, paging),
            length = assets.length;
        for (i = 0; i < length; i++) {
            assets[i].rating = rating(assets[i].id);
            assets[i].indashboard = isuserasset(assets[i].id, type);
        }
        return assets;
    };

	/**
	 * Returns asset data for the current user
	 * @param tenantId tenant id
	 * @param type Asset type
	 * @param aid Asset identifier
	 */
	asset = function(tenantId, type, aid) {
		var asset = assetManager(tenantId, type).get(aid);
		asset.rating = rating(tenantId, aid);
		return asset;
	};

	/**
	 * Returns links of a asset for the current user
	 * @param tenantId tenant id
	 * @param type Asset type
	 */
	assetLinks = function(tenantId, type) {
		var mod = require(ASSETS_EXT_PATH + type + '/asset.js'), user = require('/modules/user.js');
		return mod.assetLinks(tenantId, user);
	};

	/**
     * @param tenantId tenant id
	 * @param type
	 * @param count
	 * @return {*}
	 */
	popularAssets = function(tenantId, type, count) {
		return assetManager(tenantId, type).list({
			start : 0,
			count : count || 5,
			sort : 'popular'
		});
	};

    recentAssets = function (tenantId, type, count) {
        var i, length;

        var recent = assetManager(tenantId, type).list({
            start: 0,
            count: count || 5,
            sort: 'recent'
        });
        length = recent.length;
        for (i = 0; i < length; i++) {
            recent[i].rating = rating(recent[i].id).average;
            recent[i].indashboard = isuserasset(recent[i].id, type);
        }
        return recent;
    };

	assetCount = function(tenantId, type, options) {
		return assetManager(tenantId, type).count(options);
	};

	/**
	 * Returns all enabled asset types for the current user
	 */
    //TODO
	assetTypes = function(tenantId) {
		return configs(tenantId).assets;
	};

    //TODO:
	currentAsset = function() {
		var prefix = require('/store.js').config().assetsUrlPrefix, matcher = new URIMatcher(request.getRequestURI());
		if(matcher.match('/{context}' + prefix + '/{type}/{+any}') || matcher.match('/{context}' + prefix + '/{type}')) {
			return matcher.elements().type;
		}
		prefix = require('/store.js').config().assetUrlPrefix;
		if(matcher.match('/{context}' + prefix + '/{type}/{+any}') || matcher.match('/{context}' + prefix + '/{type}')) {
			return matcher.elements().type;
		}
		return null;
	};

	assetsPaging = function(request) {
		var page = request.getParameter('page');
		page = page ? page - 1 : 0;
		return {
			start : page * ASSETS_PAGE_SIZE,
			count : ASSETS_PAGE_SIZE,
			sort : request.getParameter('sort') || 'recent'
		};
	};

	commentsPaging = function(request) {
		var page = request.getParameter('page');
		page = page ? page - 1 : 0;
		return {
			start : page * COMMENTS_PAGE_SIZE,
			count : COMMENTS_PAGE_SIZE,
			sort : request.getParameter('sort') || 'recent'
		};
	};

    //TODO:
	cache = function(tenantId, type, key, value) {
		var cache = require('/modules/cache.js'), data = cache.cached(type) || (cache.cache(type, {}));
		return (data[key] = value);
	};

    //TODO:
	cached = function(tenantId, type, key) {
		var cache = require('/modules/cache.js'), data = cache.cached(type);
		return data ? data[key] : null;
	};

    //TODO:
	invalidate = function(tenantId, type, key) {
		var cache = require('/modules/cache.js'), data = cache.cached(type);
		delete data[key];
	};

    search = function (tenantId, options, paging) {
        var i, length, types, assets,
            type = options.type,
            attributes = options.attributes || (options.attributes = {});
        //adding status field to get only the published assets
        attributes['overview_status'] = /^(published)$/i;
        if (type) {
            var assetz = assetManager(tenantId, type).search(options, paging);
            for (i = 0; i < assetz.length; i++) {
                assetz[i].indashboard = isuserasset(assetz[i].id, type);
            }
            return assetz;
        }
        types = assetTypes();
        assets = {};
        length = types.length;
        for (i = 0; i < length; i++) {
            type = types[i];
            assets[type] = assetManager(tenantId, types[i]).search(options, paging);
        }
        return assets;
    };

    //TODO: check the logic
    isuserasset = function (tenantId, aid, type) {
        var j,
            store = require('/modules/store.js'),
            user = require('/modules/user.js').current(),
            userown = {};
        if (!user) {
            return false;
        }
        var userAssets = store.userAssets();
        if (!userAssets[type]) {
            return false;
        }
        for (j = 0; j < userAssets[type].length; j++) {
            if (userAssets[type][j]['id'] == aid) {
                userown = userAssets[type][j]['id'];
            }
        }
        return userown.length > 0;
    };

    addAsset = function (tenantId, type, options) {
        assetManager(tenantId, type).add(options);
    };

    updateAsset = function (tenantId, type, options) {
        assetManager(tenantId, type).update(options);
    };

    removeAsset = function (tenantId, type, options) {
        assetManager(tenantId, type).remove(options);
    };
}());
