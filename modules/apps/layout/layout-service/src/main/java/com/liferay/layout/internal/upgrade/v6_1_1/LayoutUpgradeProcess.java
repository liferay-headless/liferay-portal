/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.upgrade.v6_1_1;

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.segments.model.SegmentsExperience;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Adolfo Pérez
 */
public class LayoutUpgradeProcess extends UpgradeProcess {

	public LayoutUpgradeProcess(ClassNameLocalService classNameLocalService) {
		_classNameLocalService = classNameLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_upgradeLayouts();

		long layoutSetPrototypeClassNameId =
			_classNameLocalService.getClassNameId(LayoutSetPrototype.class);

		for (boolean privateLayout : new boolean[] {true, false}) {
			_upgrade(
				FragmentEntryLink.class.getName(),
				layoutSetPrototypeClassNameId, "uuid_",
				"originalFragmentEntryLinkERC", "fragmentEntryLinkId",
				privateLayout, "FragmentEntryLink");
			_upgrade(
				SegmentsExperience.class.getName(),
				layoutSetPrototypeClassNameId, "segmentsExperienceKey", null,
				"segmentsExperienceId", privateLayout, "SegmentsExperience");
		}
	}

	private String _getExternalReferenceCode(
		String className, String externalReferenceCode, boolean privateLayout) {

		if (!privateLayout) {
			return externalReferenceCode;
		}

		String newExternalReferenceCode =
			externalReferenceCode +
				LayoutConstants.EXTERNAL_REFERENCE_CODE_SUFFIX_PRIVATE;

		if (newExternalReferenceCode.length() <= ModelHintsUtil.getMaxLength(
				className, "externalReferenceCode")) {

			return newExternalReferenceCode;
		}

		return PortalUUIDUtil.generate(newExternalReferenceCode.getBytes());
	}

	private String _getSelectSQL(
		String matchColumnName, String originalExternalReferenceCodeColumnName,
		String primaryKeyColumnName, String tableName) {

		String tableName1 = tableName + "1";
		String tableName2 = tableName + "2";

		String originalExternalReferenceCodeColumnNames = StringPool.BLANK;

		if (originalExternalReferenceCodeColumnName != null) {
			originalExternalReferenceCodeColumnNames = StringBundler.concat(
				", ", tableName1, StringPool.PERIOD,
				originalExternalReferenceCodeColumnName, ", ", tableName2,
				StringPool.PERIOD, originalExternalReferenceCodeColumnName,
				" as layoutSetPrototypeOriginalERC");
		}

		return StringBundler.concat(
			"select distinct ", tableName1, ".ctCollectionId, ", tableName1,
			".uuid_, ", tableName1, ".externalReferenceCode, ", tableName1,
			StringPool.PERIOD, primaryKeyColumnName,
			originalExternalReferenceCodeColumnNames, ", ", tableName2,
			".externalReferenceCode as layoutSetPrototypeERC from ", tableName,
			StringPool.SPACE, tableName1, " inner join Layout Layout1 on ",
			"(Layout1.ctCollectionId = 0 or Layout1.ctCollectionId = ",
			tableName1, ".ctCollectionId) and Layout1.plid = ", tableName1,
			".plid inner join Layout Layout2 on (Layout2.ctCollectionId = 0 ",
			"or Layout2.ctCollectionId = ", tableName1,
			".ctCollectionId) and Layout2.companyId = Layout1.companyId and ",
			"Layout2.externalReferenceCode = ",
			"Layout1.layoutSetPrototypeLayoutERC inner join Group_ on ",
			"Group_.groupId = Layout2.groupId and Group_.classNameId = ? ",
			"inner join ", tableName, StringPool.SPACE, tableName2, " on (",
			tableName2, ".ctCollectionId = 0 or ", tableName2,
			".ctCollectionId = ", tableName1, ".ctCollectionId) and ",
			tableName2, ".groupId = Layout2.groupId and ", tableName2,
			".plid = Layout2.plid and ", tableName2, StringPool.PERIOD,
			matchColumnName, " = ", tableName1, StringPool.PERIOD,
			matchColumnName, " where Layout1.privateLayout = ? and ",
			"Layout1.layoutSetPrototypeLayoutERC is not null and ",
			"Layout1.layoutSetPrototypeLayoutERC != ''");
	}

	private String _getUpdateSQL(
		String originalExternalReferenceCodeColumnName,
		String primaryKeyColumnName, String tableName) {

		String originalExternalReferenceCodeColumnNames = StringPool.BLANK;

		if (originalExternalReferenceCodeColumnName != null) {
			originalExternalReferenceCodeColumnNames =
				", " + originalExternalReferenceCodeColumnName + " = ?";
		}

		return StringBundler.concat(
			"update ", tableName, " set externalReferenceCode = ?, uuid_ = ?",
			originalExternalReferenceCodeColumnNames,
			" where ctCollectionId = ? and ", primaryKeyColumnName, " = ?");
	}

	private void _upgrade(
			String className, long layoutSetPrototypeClassNameId,
			String matchColumnName,
			String originalExternalReferenceCodeColumnName,
			String primaryKeyColumnName, boolean privateLayout,
			String tableName)
		throws Exception {

		// The site template propagation kept the uuid of each site template
		// row and the key of each experience, so the row with the same value
		// in the site template page is the one it was propagated from.

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				_getSelectSQL(
					matchColumnName, originalExternalReferenceCodeColumnName,
					primaryKeyColumnName, tableName));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					_getUpdateSQL(
						originalExternalReferenceCodeColumnName,
						primaryKeyColumnName, tableName))) {

			preparedStatement1.setLong(1, layoutSetPrototypeClassNameId);
			preparedStatement1.setBoolean(2, privateLayout);

			Map<Long, String> uuids = new HashMap<>();

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					String externalReferenceCode = resultSet.getString(
						"externalReferenceCode");
					long primaryKey = resultSet.getLong(primaryKeyColumnName);
					String uuid = resultSet.getString("uuid_");

					String newExternalReferenceCode = _getExternalReferenceCode(
						className, resultSet.getString("layoutSetPrototypeERC"),
						privateLayout);

					String newUUID = uuid;

					if (privateLayout) {
						newUUID = uuids.computeIfAbsent(
							primaryKey, key -> PortalUUIDUtil.generate());
					}

					boolean modified = false;

					if (!Objects.equals(
							externalReferenceCode, newExternalReferenceCode) ||
						!Objects.equals(uuid, newUUID)) {

						modified = true;
					}

					int parameterIndex = 1;

					preparedStatement2.setString(
						parameterIndex++, newExternalReferenceCode);
					preparedStatement2.setString(parameterIndex++, newUUID);

					if (originalExternalReferenceCodeColumnName != null) {
						String originalExternalReferenceCode =
							resultSet.getString(
								originalExternalReferenceCodeColumnName);

						String newOriginalExternalReferenceCode =
							originalExternalReferenceCode;

						String layoutSetPrototypeOriginalERC =
							resultSet.getString(
								"layoutSetPrototypeOriginalERC");

						if (Validator.isNotNull(
								layoutSetPrototypeOriginalERC)) {

							newOriginalExternalReferenceCode =
								_getExternalReferenceCode(
									className, layoutSetPrototypeOriginalERC,
									privateLayout);
						}

						if (!Objects.equals(
								originalExternalReferenceCode,
								newOriginalExternalReferenceCode)) {

							modified = true;
						}

						preparedStatement2.setString(
							parameterIndex++, newOriginalExternalReferenceCode);
					}

					if (!modified) {
						continue;
					}

					preparedStatement2.setLong(
						parameterIndex++, resultSet.getLong("ctCollectionId"));
					preparedStatement2.setLong(parameterIndex, primaryKey);

					preparedStatement2.addBatch();
				}

				preparedStatement2.executeBatch();
			}
		}
	}

	private void _upgradeLayouts() throws Exception {

		// Update the private pages first so that the public pages can take over
		// the external reference code of the site template page.

		int maxLength = ModelHintsUtil.getMaxLength(
			Layout.class.getName(), "externalReferenceCode");

		String suffix = LayoutConstants.EXTERNAL_REFERENCE_CODE_SUFFIX_PRIVATE;

		int suffixLength = suffix.length();

		String longLayoutSetPrototypeLayoutERCPattern =
			StringPool.UNDERLINE.repeat(maxLength - suffixLength + 1) +
				StringPool.PERCENT;

		runSQL(
			StringBundler.concat(
				"update Layout set externalReferenceCode = CONCAT(",
				"layoutSetPrototypeLayoutERC, '", suffix,
				"') where privateLayout = [$TRUE$] and ",
				"layoutSetPrototypeLayoutERC is not null and ",
				"layoutSetPrototypeLayoutERC != '' and ",
				"layoutSetPrototypeLayoutERC not like '",
				longLayoutSetPrototypeLayoutERCPattern, "'"));

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select ctCollectionId, plid, layoutSetPrototypeLayoutERC " +
					"from Layout where privateLayout = ? and " +
						"layoutSetPrototypeLayoutERC like ?");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update Layout set externalReferenceCode = ? where " +
						"ctCollectionId = ? and plid = ?")) {

			preparedStatement1.setBoolean(1, true);
			preparedStatement1.setString(
				2, longLayoutSetPrototypeLayoutERCPattern);

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					preparedStatement2.setString(
						1,
						_getExternalReferenceCode(
							Layout.class.getName(),
							resultSet.getString("layoutSetPrototypeLayoutERC"),
							true));
					preparedStatement2.setLong(
						2, resultSet.getLong("ctCollectionId"));
					preparedStatement2.setLong(3, resultSet.getLong("plid"));

					preparedStatement2.addBatch();
				}

				preparedStatement2.executeBatch();
			}
		}

		runSQL(
			StringBundler.concat(
				"update Layout set externalReferenceCode = ",
				"layoutSetPrototypeLayoutERC where privateLayout = [$FALSE$] ",
				"and layoutSetPrototypeLayoutERC is not null and ",
				"layoutSetPrototypeLayoutERC != '' and externalReferenceCode ",
				"!= layoutSetPrototypeLayoutERC"));
	}

	private final ClassNameLocalService _classNameLocalService;

}