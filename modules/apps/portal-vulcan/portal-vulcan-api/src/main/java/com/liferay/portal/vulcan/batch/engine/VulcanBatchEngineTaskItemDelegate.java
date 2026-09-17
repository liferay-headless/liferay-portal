/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.batch.engine;

import com.liferay.petra.function.UnsafeBiConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.fields.NestedFieldsContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import jakarta.ws.rs.core.UriInfo;

import java.io.Serializable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Javier Gamarra
 */
@ProviderType
public interface VulcanBatchEngineTaskItemDelegate<T> {

	public void create(
			Collection<T> items, Map<String, Serializable> parameters)
		throws Exception;

	public default NestedFieldsContext customizeNestedFieldsContext(
		NestedFieldsContext nestedFieldsContext) {

		return nestedFieldsContext;
	}

	public void delete(
			Collection<T> items, Map<String, Serializable> parameters)
		throws Exception;

	public default Set<String> getAvailableCreateStrategies() {
		return null;
	}

	public default Set<String> getAvailableUpdateStrategies() {
		return null;
	}

	public EntityModel getEntityModel(Map<String, List<String>> multivaluedMap)
		throws Exception;

	public default Class<?> getResourceClass() {
		return getClass();
	}

	public default String getResourceName() {
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
		return "v1.0";
	}

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

	public void setContextBatchUnsafeBiConsumer(
		UnsafeBiConsumer
			<Collection<T>, UnsafeFunction<T, T, Exception>, Exception>
				contextBatchUnsafeBiConsumer);

	public void setContextCompany(Company contextCompany);

	public void setContextUriInfo(UriInfo uriInfo);

	public void setContextUser(User contextUser);

	public void setGroupLocalService(GroupLocalService groupLocalService);

	public void setLanguageId(String languageId);

	public void setResourceActionLocalService(
		ResourceActionLocalService resourceActionLocalService);

	public void setResourcePermissionLocalService(
		ResourcePermissionLocalService resourcePermissionLocalService);

	public void setRoleLocalService(RoleLocalService roleLocalService);

	public void update(
			Collection<T> items, Map<String, Serializable> parameters)
		throws Exception;

}