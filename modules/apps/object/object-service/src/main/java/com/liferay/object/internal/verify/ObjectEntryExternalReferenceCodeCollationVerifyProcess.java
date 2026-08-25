/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.verify;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.dao.db.IndexMetadata;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.verify.VerifyProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * Forces the ObjectEntry externalReferenceCode column, and therefore its unique
 * index, to a case sensitive collation on databases whose collation is case
 * insensitive.
 *
 * <p>
 * The ObjectEntry finders and the unique index on externalReferenceCode compare
 * the value case sensitively by design. On a case insensitive database
 * collation the index folds case, so two codes that differ only in case, such
 * as "ERC1" and "erc1", collide and the second insert is rejected. Liferay's
 * DDL layer emits no COLLATE clause, so the collation must be forced out of
 * band.
 * </p>
 *
 * <p>
 * This is a verify process rather than an upgrade step so that it runs on both a
 * fresh install and an upgrade: the "initial.deployment=true" property runs it
 * after the tables are created on a fresh install, and the unverified release
 * runs it after an upgrade.
 * </p>
 *
 * @author Alejandro Tardín
 */
@Component(property = "initial.deployment=true", service = VerifyProcess.class)
public class ObjectEntryExternalReferenceCodeCollationVerifyProcess
	extends VerifyProcess {

	@Override
	protected void doVerify() throws Exception {
		DB db = DBManagerUtil.getDB();

		// Databases that already compare strings case sensitively, the
		// supported configuration, need no change.

		if (db.isSupportsStringCaseSensitiveQuery() ||
			!hasColumn(_TABLE_NAME, _COLUMN_NAME)) {

			return;
		}

		DBType dbType = db.getDBType();

		String alterColumnSQL = null;

		if ((dbType == DBType.MARIADB) || (dbType == DBType.MYSQL)) {
			alterColumnSQL = StringBundler.concat(
				"alter table ", _TABLE_NAME, " modify ", _COLUMN_NAME,
				" varchar(500) character set utf8mb4 collate utf8mb4_bin");
		}
		else if (dbType == DBType.SQLSERVER) {
			alterColumnSQL = StringBundler.concat(
				"alter table ", _TABLE_NAME, " alter column ", _COLUMN_NAME,
				" nvarchar(500) collate Latin1_General_CS_AS");
		}
		else {

			// Other engines that can be case insensitive, such as a PostgreSQL
			// database created with a nondeterministic ICU collation, would
			// need their own statement here.

			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to force a case sensitive collation on ",
						_TABLE_NAME, ".", _COLUMN_NAME,
						" for case insensitive database type ", dbType));
			}

			return;
		}

		if (_isCaseSensitiveColumn(dbType)) {
			return;
		}

		// Drop the indexes on the column, alter its collation, then restore the
		// indexes so the unique index is rebuilt under the new collation. This
		// mirrors BaseIndexedColumnSizeUpgradeProcess and works on every
		// database, including those that forbid altering an indexed column.

		List<IndexMetadata> indexMetadatas = dropIndexes(
			_TABLE_NAME, _COLUMN_NAME);

		runSQL(alterColumnSQL);

		addIndexes(connection, indexMetadatas);
	}

	private boolean _isCaseSensitiveColumn(DBType dbType) throws Exception {
		String sql = null;

		if ((dbType == DBType.MARIADB) || (dbType == DBType.MYSQL)) {
			sql = StringBundler.concat(
				"select collation_name from information_schema.columns where ",
				"table_schema = database() and table_name = '", _TABLE_NAME,
				"' and column_name = '", _COLUMN_NAME, "'");
		}
		else if (dbType == DBType.SQLSERVER) {
			sql = StringBundler.concat(
				"select collation_name from sys.columns where object_id = ",
				"object_id('", _TABLE_NAME, "') and name = '", _COLUMN_NAME,
				"'");
		}
		else {
			return false;
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				sql);

			ResultSet resultSet = preparedStatement.executeQuery()) {

			if (!resultSet.next()) {
				return false;
			}

			String collationName = resultSet.getString(1);

			if (collationName == null) {
				return false;
			}

			collationName = StringUtil.toLowerCase(collationName);

			// A MySQL or MariaDB collation is case insensitive only when it
			// ends with "_ci". A SQL Server collation is case sensitive only
			// when it contains "_cs".

			if ((dbType == DBType.SQLSERVER) && collationName.contains("_cs")) {
				return true;
			}

			if ((dbType != DBType.SQLSERVER) &&
				!collationName.endsWith("_ci")) {

				return true;
			}

			return false;
		}
	}

	private static final String _COLUMN_NAME = "externalReferenceCode";

	private static final String _TABLE_NAME = "ObjectEntry";

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryExternalReferenceCodeCollationVerifyProcess.class);

}