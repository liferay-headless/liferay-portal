/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

import com.liferay.object.constants.ObjectActionTriggerConstants;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.model.ObjectAction;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.relationship.util.ObjectRelationshipUtil;
import com.liferay.object.service.ObjectActionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.object.system.JaxRsApplicationDescriptor;
import com.liferay.object.system.SystemObjectDefinitionManager;
import com.liferay.object.system.SystemObjectDefinitionManagerRegistry;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.application.HeadlessApplicationProvider;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Jose Luis Navarro
 */
public class NumberOfToolsUtil {

	public static int getNumberOfTools(
		String apiPath, long companyId,
		HeadlessApplicationProvider.OpenAPIDocument openAPIDocument) {

		HeadlessApplicationProvider.Application application =
			openAPIDocument.getApplication();

		// The OpenAPI document merges the methods that share a path and an
		// HTTP method into one operation

		Set<String> operations = new HashSet<>();

		for (HeadlessApplicationProvider.ResourceMethod resourceMethod :
				application.getResourceMethods()) {

			String method = resourceMethod.getMethod();
			String path = resourceMethod.getPath();

			if ((method == null) || (path == null) ||
				!path.startsWith(apiPath)) {

				continue;
			}

			operations.add(
				method + StringPool.SPACE + path.substring(apiPath.length()));
		}

		ObjectDefinition objectDefinition = _fetchObjectDefinition(
			companyId, application.getBasePath());

		if (objectDefinition != null) {
			return _getObjectDefinitionNumberOfTools(
				companyId, objectDefinition, operations);
		}

		// The object definition deployer registers relationship template
		// paths on the application of every system object definition, which
		// RelatedObjectEntryOpenAPIContributor expands once per relationship

		int numberOfObjectRelationshipTemplates = 0;
		int numberOfTools = 0;

		for (String operation : operations) {
			if (operation.contains("{objectRelationshipName")) {
				numberOfObjectRelationshipTemplates++;
			}
			else {
				numberOfTools++;
			}
		}

		int numberOfSystemObjectRelationships =
			_getNumberOfSystemObjectRelationships(application.getBasePath());

		numberOfTools +=
			numberOfObjectRelationshipTemplates *
				numberOfSystemObjectRelationships;

		return numberOfTools;
	}

	private static ObjectDefinition _fetchObjectDefinition(
		long companyId, String restContextPath) {

		ObjectDefinitionLocalService objectDefinitionLocalService =
			_objectDefinitionLocalServiceSnapshot.get();

		for (ObjectDefinition objectDefinition :
				objectDefinitionLocalService.getObjectDefinitions(
					companyId, true, WorkflowConstants.STATUS_APPROVED)) {

			if (!objectDefinition.isUnmodifiableSystemObject() &&
				Objects.equals(
					restContextPath, objectDefinition.getRESTContextPath())) {

				return objectDefinition;
			}
		}

		return null;
	}

	private static Set<String> _getExcludedOperationIds(
		long companyId, String path) {

		// The object definition deployer stores the operations it excludes in
		// the Vulcan configuration of the application, keyed by operation ID

		ConfigurationAdmin configurationAdmin =
			_configurationAdminSnapshot.get();

		try {
			Configuration[] configurations =
				configurationAdmin.listConfigurations(
					StringBundler.concat(
						"(&(path=", path, ")(|(service.factoryPid=",
						_VULCAN_CONFIGURATION_PID, ")(&(service.factoryPid=",
						_VULCAN_COMPANY_CONFIGURATION_PID, ")(companyId=",
						companyId, "))))"));

			if (ArrayUtil.isEmpty(configurations)) {
				return Collections.emptySet();
			}

			Dictionary<String, Object> properties =
				configurations[0].getProperties();

			return SetUtil.fromArray(
				StringUtil.split(
					GetterUtil.getString(
						properties.get("excludedOperationIds"))));
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}

			return Collections.emptySet();
		}
	}

	private static int _getNumberOfObjectRelationships(
		ObjectDefinition objectDefinition) {

		// Mirror ObjectEntryOpenAPIContributor, which expands each
		// relationship template path once per many to many relationship and
		// once per one to many relationship that the object definition owns

		int numberOfObjectRelationships = 0;

		ObjectRelationshipLocalService objectRelationshipLocalService =
			_objectRelationshipLocalServiceSnapshot.get();

		for (ObjectRelationship objectRelationship :
				objectRelationshipLocalService.getAllObjectRelationships(
					objectDefinition.getObjectDefinitionId())) {

			ObjectDefinition relatedObjectDefinition =
				ObjectRelationshipUtil.getRelatedObjectDefinition(
					objectDefinition, objectRelationship);

			if (!relatedObjectDefinition.isActive()) {
				continue;
			}

			String type = objectRelationship.getType();

			if (Objects.equals(
					type, ObjectRelationshipConstants.TYPE_MANY_TO_MANY) ||
				(Objects.equals(
					type, ObjectRelationshipConstants.TYPE_ONE_TO_MANY) &&
				 (objectRelationship.getObjectDefinitionId1() ==
					 objectDefinition.getObjectDefinitionId()))) {

				numberOfObjectRelationships++;
			}
		}

		return numberOfObjectRelationships;
	}

	private static int _getNumberOfSystemObjectRelationships(String basePath) {

		// Mirror RelatedObjectEntryOpenAPIContributor, which expands the
		// templates once per relationship of the system object definitions
		// that live on the application. The relationships of every company
		// land on one document, so equally named relationships expand the
		// templates once.

		ObjectDefinitionLocalService objectDefinitionLocalService =
			_objectDefinitionLocalServiceSnapshot.get();
		ObjectRelationshipLocalService objectRelationshipLocalService =
			_objectRelationshipLocalServiceSnapshot.get();
		SystemObjectDefinitionManagerRegistry
			systemObjectDefinitionManagerRegistry =
				_systemObjectDefinitionManagerRegistrySnapshot.get();

		Set<String> relationshipPaths = new HashSet<>();

		for (ObjectDefinition systemObjectDefinition :
				objectDefinitionLocalService.getSystemObjectDefinitions()) {

			SystemObjectDefinitionManager systemObjectDefinitionManager =
				systemObjectDefinitionManagerRegistry.
					getSystemObjectDefinitionManager(
						systemObjectDefinition.getName());

			if (systemObjectDefinitionManager == null) {
				continue;
			}

			JaxRsApplicationDescriptor jaxRsApplicationDescriptor =
				systemObjectDefinitionManager.getJaxRsApplicationDescriptor();

			if (!Objects.equals(
					basePath,
					StringPool.SLASH +
						jaxRsApplicationDescriptor.getApplicationPath())) {

				continue;
			}

			for (ObjectRelationship objectRelationship :
					objectRelationshipLocalService.getObjectRelationships(
						systemObjectDefinition.getObjectDefinitionId())) {

				ObjectDefinition relatedObjectDefinition =
					ObjectRelationshipUtil.getRelatedObjectDefinition(
						systemObjectDefinition, objectRelationship);

				if (relatedObjectDefinition.isActive()) {
					relationshipPaths.add(
						StringBundler.concat(
							jaxRsApplicationDescriptor.getPath(),
							StringPool.SLASH, objectRelationship.getName()));
				}
			}
		}

		return relationshipPaths.size();
	}

	private static int _getObjectDefinitionNumberOfTools(
		long companyId, ObjectDefinition objectDefinition,
		Set<String> operations) {

		// The application serves its OpenAPI document itself rather than
		// through a resource, so that operation is not among the resource
		// methods

		int numberOfTools = 1;

		int numberOfObjectActionTemplates = 0;
		int numberOfObjectRelationships = _getNumberOfObjectRelationships(
			objectDefinition);

		for (String operation : operations) {
			String path = StringUtil.extractLast(operation, CharPool.SPACE);

			if (path.contains("{objectActionName}")) {
				numberOfObjectActionTemplates++;
			}
			else if (path.contains("{objectRelationshipName}")) {
				numberOfTools += numberOfObjectRelationships;
			}
			else {
				numberOfTools++;
			}
		}

		// Every excluded operation ID names one of the operations above, and
		// the IDs of the action template operations carry the name of the
		// template's path parameter

		for (String excludedOperationId :
				_getExcludedOperationIds(
					companyId, objectDefinition.getRESTContextPath())) {

			if (excludedOperationId.contains("ObjectActionObjectActionName")) {
				numberOfObjectActionTemplates--;
			}
			else {
				numberOfTools--;
			}
		}

		ObjectActionLocalService objectActionLocalService =
			_objectActionLocalServiceSnapshot.get();

		List<ObjectAction> objectActions =
			objectActionLocalService.getObjectActions(
				objectDefinition.getObjectDefinitionId(),
				ObjectActionTriggerConstants.KEY_STANDALONE);

		return numberOfTools +
			(numberOfObjectActionTemplates * objectActions.size());
	}

	private static final String _VULCAN_COMPANY_CONFIGURATION_PID =
		"com.liferay.portal.vulcan.internal.configuration." +
			"VulcanCompanyConfiguration";

	private static final String _VULCAN_CONFIGURATION_PID =
		"com.liferay.portal.vulcan.internal.configuration.VulcanConfiguration";

	private static final Log _log = LogFactoryUtil.getLog(
		NumberOfToolsUtil.class);

	private static final Snapshot<ConfigurationAdmin>
		_configurationAdminSnapshot = new Snapshot<>(
			NumberOfToolsUtil.class, ConfigurationAdmin.class);
	private static final Snapshot<ObjectActionLocalService>
		_objectActionLocalServiceSnapshot = new Snapshot<>(
			NumberOfToolsUtil.class, ObjectActionLocalService.class);
	private static final Snapshot<ObjectDefinitionLocalService>
		_objectDefinitionLocalServiceSnapshot = new Snapshot<>(
			NumberOfToolsUtil.class, ObjectDefinitionLocalService.class);
	private static final Snapshot<ObjectRelationshipLocalService>
		_objectRelationshipLocalServiceSnapshot = new Snapshot<>(
			NumberOfToolsUtil.class, ObjectRelationshipLocalService.class);
	private static final Snapshot<SystemObjectDefinitionManagerRegistry>
		_systemObjectDefinitionManagerRegistrySnapshot = new Snapshot<>(
			NumberOfToolsUtil.class,
			SystemObjectDefinitionManagerRegistry.class);

}