package com.liferay.headless.admin.content.internal.graphql.extension;

import com.liferay.portal.vulcan.graphql.contributor.GraphQLContributor;

/**
 * @author Luis Miguel Barcos
 */
public class StructuredContentGraphQLContributor implements GraphQLContributor {
	@Override
	public String getPath() {
		return "/headless-admin-content/v1_0";
	}
}
