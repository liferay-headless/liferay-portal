/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.upgrade.v6_1_1;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alberto Chaparro
 */
public class LayoutLayoutSetPrototypeLayoutERCUpgradeProcess
	extends UpgradeProcess {

	public LayoutLayoutSetPrototypeLayoutERCUpgradeProcess(
		ClassNameLocalService classNameLocalService) {

		_classNameLocalService = classNameLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		Map<String, String> parentLayoutSetPrototypeLayoutERCs =
			_getParentLayoutSetPrototypeLayoutERCs();

		// Pages created as the parent of a page imported by the site template
		// sync took the site template page code of that page, so give them the
		// code of their own site template page.

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select distinct Layout1.companyId, Layout1.plid, ",
					"Layout1.layoutSetPrototypeLayoutERC from Layout Layout1 ",
					"inner join Layout Layout2 on Layout2.groupId = ",
					"Layout1.groupId and Layout2.privateLayout = ",
					"Layout1.privateLayout and Layout2.parentLayoutId = ",
					"Layout1.layoutId and (Layout2.ctCollectionId = 0 or ",
					"Layout2.ctCollectionId = Layout1.ctCollectionId) and ",
					"Layout2.layoutSetPrototypeLayoutERC = ",
					"Layout1.layoutSetPrototypeLayoutERC where ",
					"Layout1.layoutSetPrototypeLayoutERC is not null and ",
					"Layout1.layoutSetPrototypeLayoutERC != ''"));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update Layout set layoutSetPrototypeLayoutERC = ? where " +
						"plid = ? and layoutSetPrototypeLayoutERC = ?");
			ResultSet resultSet = preparedStatement1.executeQuery()) {

			while (resultSet.next()) {
				long plid = resultSet.getLong("plid");

				String layoutSetPrototypeLayoutERC = resultSet.getString(
					"layoutSetPrototypeLayoutERC");

				String parentLayoutSetPrototypeLayoutERC =
					parentLayoutSetPrototypeLayoutERCs.get(
						StringBundler.concat(
							resultSet.getLong("companyId"), StringPool.POUND,
							layoutSetPrototypeLayoutERC));

				if (parentLayoutSetPrototypeLayoutERC == null) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							StringBundler.concat(
								"Unable to find the parent of the site ",
								"template page ", layoutSetPrototypeLayoutERC,
								" to fix page ", plid));
					}

					parentLayoutSetPrototypeLayoutERC = StringPool.BLANK;
				}

				preparedStatement2.setString(
					1, parentLayoutSetPrototypeLayoutERC);
				preparedStatement2.setLong(2, plid);
				preparedStatement2.setString(3, layoutSetPrototypeLayoutERC);

				preparedStatement2.addBatch();
			}

			preparedStatement2.executeBatch();
		}
	}

	private Map<String, String> _getParentLayoutSetPrototypeLayoutERCs()
		throws Exception {

		Map<String, String> parentLayoutSetPrototypeLayoutERCs =
			new HashMap<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select Layout1.companyId, Layout1.externalReferenceCode ",
					"as layoutSetPrototypeLayoutERC, ",
					"Layout2.externalReferenceCode as ",
					"parentLayoutSetPrototypeLayoutERC from Layout Layout1 ",
					"inner join Group_ on Group_.groupId = Layout1.groupId ",
					"and Group_.classNameId = ? inner join Layout Layout2 on ",
					"Layout2.groupId = Layout1.groupId and ",
					"Layout2.privateLayout = Layout1.privateLayout and ",
					"Layout2.layoutId = Layout1.parentLayoutId and ",
					"Layout2.ctCollectionId = Layout1.ctCollectionId where ",
					"Layout1.ctCollectionId = 0"))) {

			preparedStatement.setLong(
				1,
				_classNameLocalService.getClassNameId(
					LayoutSetPrototype.class));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					parentLayoutSetPrototypeLayoutERCs.put(
						StringBundler.concat(
							resultSet.getLong("companyId"), StringPool.POUND,
							resultSet.getString("layoutSetPrototypeLayoutERC")),
						resultSet.getString(
							"parentLayoutSetPrototypeLayoutERC"));
				}
			}
		}

		return parentLayoutSetPrototypeLayoutERCs;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LayoutLayoutSetPrototypeLayoutERCUpgradeProcess.class);

	private final ClassNameLocalService _classNameLocalService;

}