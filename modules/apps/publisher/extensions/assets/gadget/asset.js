var assetLinks = function (user) {
    return {
        title: 'Gadget',
        links: [
            {
                title: 'Bookmarks',
                url: 'bookmarks',
                path: 'bookmarks.jag'
            }
        ],
        isCategorySupport: true
    };
};


var apiLinks = function (user) {
    return {
        links: [
            {
                url: 'docs',
                path: 'doc_api.jag'
            }
        ]
    };
};