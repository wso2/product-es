(function() {
	'use strict';

	var DEFAULT_CLAIM_DIALECT = null;

	var getClaims = function(dialect) {
		var claims = [];

		var handler = Packages.org.wso2.carbon.claim.mgt.ClaimManagerHandler.getInstance();

		var defaultClaims = handler.getAllSupportedClaimMappings(dialect);

		for (var i = 0; i < defaultClaims.length; i++) {
			var c = defaultClaims[i].getClaim();

			var claim = {
				claimUri: c.getClaimUri(),
				displayTag: c.getDisplayTag(),
				isRequired: c.isRequired(),
				regex: c.getRegEx(),
				value: c.getValue(),
				displayOrder: c.getDisplayOrder()
			};

			claims.push(claim);
		}

		return claims;
	};


	this.getDefaultClaims = function() {
		if (DEFAULT_CLAIM_DIALECT) {
			return getClaims(DEFAULT_CLAIM_DIALECT);
		} else {
			return [];
		}
	};

	this.getClaims = function (dialect) {
		return getClaims(dialect);
	};
})();