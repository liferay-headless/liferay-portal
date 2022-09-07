/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.vulcan.internal.graphql.data.fetcher;

import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.vulcan.graphql.validation.GraphQLRequestContext;
import com.liferay.portal.vulcan.graphql.validation.GraphQLRequestContextValidator;
import com.liferay.portal.vulcan.internal.graphql.constants.GraphQLConstants;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLType;

import graphql.servlet.GraphQLContext;

import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Carlos Correa
 */
public abstract class BaseDataFetcher implements DataFetcher<Object> {

	public BaseDataFetcher(
		GraphQLRequestContext graphQLRequestContext,
		ServiceTrackerList<GraphQLRequestContextValidator>
			graphQLRequestContextValidators) {

		_graphQLRequestContext = graphQLRequestContext;
		_graphQLRequestContextValidators = graphQLRequestContextValidators;
	}

	@Override
	public final Object get(DataFetchingEnvironment dataFetchingEnvironment)
		throws Exception {

		try {
			HttpServletRequest httpServletRequest = _getHttpServletRequest(
				dataFetchingEnvironment);

			GraphQLType graphQLType = dataFetchingEnvironment.getParentType();

			if (_graphQLNamespaces.contains(graphQLType.getName())) {
				for (GraphQLRequestContextValidator
						graphQLRequestContextValidator :
							_graphQLRequestContextValidators) {

					graphQLRequestContextValidator.validate(
						_graphQLRequestContext);
				}
			}

			return get(
				dataFetchingEnvironment, httpServletRequest,
				_getHttpServletResponse(dataFetchingEnvironment));
		}
		catch (InvocationTargetException invocationTargetException) {
			if (_log.isWarnEnabled()) {
				_log.warn(invocationTargetException);
			}

			throw new RuntimeException(
				invocationTargetException.getTargetException());
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}

			throw new RuntimeException(exception);
		}
	}

	public abstract Object get(
			DataFetchingEnvironment dataFetchingEnvironment,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception;

	private HttpServletRequest _getHttpServletRequest(
		DataFetchingEnvironment dataFetchingEnvironment) {

		GraphQLContext graphQLContext = dataFetchingEnvironment.getContext();

		Optional<HttpServletRequest> httpServletRequestOptional =
			graphQLContext.getHttpServletRequest();

		return httpServletRequestOptional.orElse(null);
	}

	private HttpServletResponse _getHttpServletResponse(
		DataFetchingEnvironment dataFetchingEnvironment) {

		GraphQLContext graphQLContext = dataFetchingEnvironment.getContext();

		Optional<HttpServletResponse> httpServletResponseOptional =
			graphQLContext.getHttpServletResponse();

		return httpServletResponseOptional.orElse(null);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BaseDataFetcher.class);

	private static final List<String> _graphQLNamespaces = Arrays.asList(
		GraphQLConstants.NAMESPACE_C, GraphQLConstants.NAMESPACE_MUTATION,
		GraphQLConstants.NAMESPACE_QUERY);

	private final GraphQLRequestContext _graphQLRequestContext;
	private final ServiceTrackerList<GraphQLRequestContextValidator>
		_graphQLRequestContextValidators;

}