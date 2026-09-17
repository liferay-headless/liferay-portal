/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine;

import com.liferay.batch.engine.pagination.Page;
import com.liferay.batch.engine.pagination.Pagination;
import com.liferay.petra.function.UnsafeBiConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.odata.entity.EntityModel;

import jakarta.ws.rs.core.UriInfo;

import java.io.Serializable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Ivica Cardic
 */
@ProviderType
public interface BatchEngineTaskItemDelegate<T> {

	public void create(
			Collection<T> items, Map<String, Serializable> parameters)
		throws Exception;

	public void delete(
			Collection<T> items, Map<String, Serializable> parameters)
		throws Exception;

	public Set<String> getAvailableCreateStrategies();

	public Set<String> getAvailableUpdateStrategies();

	public EntityModel getEntityModel(Map<String, List<String>> multivaluedMap)
		throws Exception;

	public default Class<T> getItemClass() {
		return null;
	}

	/**
	 * Returns the transaction propagation each item of this delegate runs
	 * under. The default nested savepoint lets a failing item roll back
	 * without poisoning the shared connection. Return
	 * {@link Propagation#NOT_SUPPORTED} when an item issues DDL, because an
	 * implicit commit discards the savepoint the executor relies on.
	 */
	public default Propagation getTransactionPropagation() {
		return Propagation.NESTED;
	}

	public default String getVersion() {
		return null;
	}

	public boolean hasCreateStrategy(String createStrategy);

	public boolean hasUpdateStrategy(String updateStrategy);

	/**
	 * Returns <code>true</code> if the items of this delegate must run in
	 * batch mode. Batch mode defers search indexing and makes model listeners
	 * skip work that is redone once the import finishes, which suits importing
	 * plain records in bulk. Return <code>false</code> when a single item is a
	 * composite operation that depends on those listeners and on indexing.
	 */
	public default boolean isBatchModeEnabled() {
		return true;
	}

	public Page<T> read(
			Filter filter, Pagination pagination, Sort[] sorts,
			Map<String, Serializable> parameters, String search)
		throws Exception;

	public void setContextCompany(Company contextCompany);

	public void setContextUriInfo(UriInfo uriInfo);

	public void setContextUser(User contextUser);

	public void setImportItemUnsafeBiConsumer(
		UnsafeBiConsumer<T, UnsafeFunction<T, T, Exception>, Exception>
			unsafeBiConsumer);

	public void setLanguageId(String languageId);

	public void update(
			Collection<T> items, Map<String, Serializable> parameters)
		throws Exception;

}