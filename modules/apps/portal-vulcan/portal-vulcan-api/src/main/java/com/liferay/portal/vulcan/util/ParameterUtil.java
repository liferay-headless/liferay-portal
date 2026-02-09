/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.util;

import com.liferay.changeset.model.ChangesetEntry;
import com.liferay.changeset.service.ChangesetEntryLocalServiceUtil;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.petra.function.UnsafeBiFunction;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ExternalReferenceCodeModel;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Petteri Karttunen
 */
public class ParameterUtil {

	public static String buildFilterParameterFromChangeset(
		UnsafeBiFunction
			<String, Long, ExternalReferenceCodeModel, PortalException>
				unsafeBiFunction,
		String modelClassName, PortletDataContext portletDataContext) {

		long changesetCollectionId = MapUtil.getLong(
			portletDataContext.getParameterMap(), "changesetCollectionId");

		if (changesetCollectionId == 0) {
			return null;
		}

		Set<String> externalReferenceCodes = new HashSet<>();

		externalReferenceCodes.add("");

		List<ChangesetEntry> changesetEntries =
			ChangesetEntryLocalServiceUtil.getChangesetEntries(
				changesetCollectionId,
				ClassNameLocalServiceUtil.getClassNameId(modelClassName));

		for (ChangesetEntry changesetEntry : changesetEntries) {
			try {
				ExternalReferenceCodeModel externalReferenceCodeModel =
					unsafeBiFunction.apply(
						changesetEntry.getClassExternalReferenceCode(),
						changesetEntry.getGroupId());

				if (externalReferenceCodeModel != null) {
					externalReferenceCodes.add(
						externalReferenceCodeModel.getExternalReferenceCode());
				}
			}
			catch (Exception exception) {
				_log.error(exception);
			}
		}

		return StringBundler.concat(
			"externalReferenceCode in (",
			StringUtil.merge(
				TransformUtil.transform(
					externalReferenceCodes,
					layoutExternalReferenceCode ->
						"'" + layoutExternalReferenceCode + "'")),
			")");
	}

	private static final Log _log = LogFactoryUtil.getLog(ParameterUtil.class);

}