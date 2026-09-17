/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.entry.type;

import com.liferay.item.selector.ItemSelectorCriterion;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;

import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Alejandro Tardín
 * @author David Truong
 */
@ProviderType
public interface LaunchEntryType {

	public Version addPublishedVersion(long classPK, String classVersion)
		throws PortalException;

	public Version addVersion(long classPK, String classVersion)
		throws PortalException;

	public Version fetchPublishedVersion(long classPK) throws PortalException;

	public Version fetchVersion(long classPK, String classVersion)
		throws PortalException;

	public long getClassPK(Map<String, String> selectedItemData)
		throws PortalException;

	public ItemSelectorCriterion getItemSelectorCriterion();

	public String getLabel(Locale locale);

	public Class<?> getModelClass();

	public List<Version> getVersions(long classPK, Locale locale)
		throws PortalException;

	public Version publishVersion(long classPK, String classVersion)
		throws PortalException;

	public interface Version {

		public String getClassVersion();

		public String getEditURL(
				String redirect,
				RequestBackedPortletURLFactory requestBackedPortletURLFactory)
			throws PortalException;

		public long getGroupId();

		public String getLabel(Locale locale);

		public Date getModifiedDate();

		public BaseModel<?> getPreviewBaseModel() throws PortalException;

		public Serializable getPrimaryKey();

		public int getStatus();

		public String getTitle(Locale locale);

		public String getTypeName(Locale locale) throws PortalException;

		public String getUserName();

	}

}