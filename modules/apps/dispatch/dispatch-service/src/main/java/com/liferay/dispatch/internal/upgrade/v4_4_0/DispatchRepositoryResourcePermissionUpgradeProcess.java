/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dispatch.internal.upgrade.v4_4_0;

import com.liferay.dispatch.constants.DispatchPortletKeys;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RepositoryLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.util.Objects;

/**
 * @author Adolfo Pérez
 */
public class DispatchRepositoryResourcePermissionUpgradeProcess
	extends UpgradeProcess {

	public DispatchRepositoryResourcePermissionUpgradeProcess(
		CompanyLocalService companyLocalService,
		DLFileEntryLocalService dlFileEntryLocalService,
		DLFolderLocalService dlFolderLocalService,
		GroupLocalService groupLocalService,
		RepositoryLocalService repositoryLocalService,
		ResourcePermissionLocalService resourcePermissionLocalService,
		RoleLocalService roleLocalService, UserLocalService userLocalService) {

		_companyLocalService = companyLocalService;
		_dlFileEntryLocalService = dlFileEntryLocalService;
		_dlFolderLocalService = dlFolderLocalService;
		_groupLocalService = groupLocalService;
		_repositoryLocalService = repositoryLocalService;
		_resourcePermissionLocalService = resourcePermissionLocalService;
		_roleLocalService = roleLocalService;
		_userLocalService = userLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompany(
			company -> _upgradeCompany(company.getCompanyId()));
	}

	private void _removeResourcePermissions(
			long companyId, long guestUserId, String name, long primKey,
			Role... roles)
		throws PortalException {

		for (Role role : roles) {
			if (Objects.equals(role.getName(), RoleConstants.OWNER)) {
				ResourcePermission resourcePermission =
					_resourcePermissionLocalService.fetchResourcePermission(
						companyId, name, ResourceConstants.SCOPE_INDIVIDUAL,
						String.valueOf(primKey), role.getRoleId());

				if ((resourcePermission == null) ||
					(resourcePermission.getOwnerId() != guestUserId)) {

					continue;
				}
			}

			_resourcePermissionLocalService.setResourcePermissions(
				companyId, name, ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(primKey), role.getRoleId(), new String[0]);
		}
	}

	private void _upgradeCompany(long companyId) throws Exception {
		Group group = _groupLocalService.getCompanyGroup(companyId);

		Repository repository = _repositoryLocalService.fetchRepository(
			group.getGroupId(), DispatchPortletKeys.DISPATCH);

		if (repository == null) {
			return;
		}

		long guestUserId = _userLocalService.getGuestUserId(companyId);

		Role guestRole = _roleLocalService.getRole(
			companyId, RoleConstants.GUEST);
		Role ownerRole = _roleLocalService.getRole(
			companyId, RoleConstants.OWNER);
		Role userRole = _roleLocalService.getRole(
			companyId, RoleConstants.USER);

		_removeResourcePermissions(
			companyId, guestUserId, DLFolder.class.getName(),
			repository.getDlFolderId(), guestRole, ownerRole, userRole);

		ActionableDynamicQuery actionableDynamicQuery1 =
			_dlFolderLocalService.getActionableDynamicQuery();

		actionableDynamicQuery1.setAddCriteriaMethod(
			dynamicQuery -> dynamicQuery.add(
				RestrictionsFactoryUtil.eq(
					"repositoryId", repository.getRepositoryId())));
		actionableDynamicQuery1.setCompanyId(companyId);
		actionableDynamicQuery1.setPerformActionMethod(
			(DLFolder dlFolder) -> _removeResourcePermissions(
				companyId, guestUserId, DLFolder.class.getName(),
				dlFolder.getFolderId(), guestRole, ownerRole, userRole));

		actionableDynamicQuery1.performActions();

		ActionableDynamicQuery actionableDynamicQuery2 =
			_dlFileEntryLocalService.getActionableDynamicQuery();

		actionableDynamicQuery2.setAddCriteriaMethod(
			dynamicQuery -> dynamicQuery.add(
				RestrictionsFactoryUtil.eq(
					"repositoryId", repository.getRepositoryId())));
		actionableDynamicQuery2.setCompanyId(companyId);
		actionableDynamicQuery2.setPerformActionMethod(
			(DLFileEntry dlFileEntry) -> _removeResourcePermissions(
				companyId, guestUserId, DLFileEntry.class.getName(),
				dlFileEntry.getFileEntryId(), guestRole, ownerRole, userRole));

		actionableDynamicQuery2.performActions();
	}

	private final CompanyLocalService _companyLocalService;
	private final DLFileEntryLocalService _dlFileEntryLocalService;
	private final DLFolderLocalService _dlFolderLocalService;
	private final GroupLocalService _groupLocalService;
	private final RepositoryLocalService _repositoryLocalService;
	private final ResourcePermissionLocalService
		_resourcePermissionLocalService;
	private final RoleLocalService _roleLocalService;
	private final UserLocalService _userLocalService;

}