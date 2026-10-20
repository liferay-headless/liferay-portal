/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.upgrade.v6_1_1;

import com.liferay.change.tracking.constants.CTConstants;
import com.liferay.fragment.model.FragmentEntryLinkTable;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.model.LayoutTable;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.segments.model.SegmentsExperienceTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
		long layoutClassNameId = _classNameLocalService.getClassNameId(
			Layout.class);
		long layoutSetPrototypeClassNameId =
			_classNameLocalService.getClassNameId(LayoutSetPrototype.class);

		List<Long> ctCollectionIds = _getCTCollectionIds();

		for (long ctCollectionId : ctCollectionIds) {
			for (boolean privateLayout : new boolean[] {true, false}) {
				for (LayoutSetInfo layoutSetInfo :
						_getLayoutSetInfos(
							ctCollectionId, layoutSetPrototypeClassNameId,
							privateLayout)) {

					_upgradeLayoutSet(
						ctCollectionId, ctCollectionIds, layoutClassNameId,
						layoutSetInfo);
				}
			}
		}
	}

	private List<Long> _getCTCollectionIds() throws Exception {
		List<Long> ctCollectionIds = new ArrayList<>();

		ctCollectionIds.add(CTConstants.CT_COLLECTION_ID_PRODUCTION);

		if (!hasTable("CTCollection")) {
			return ctCollectionIds;
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select ctCollectionId from CTCollection where status = ? or " +
					"status = ? or status = ? order by ctCollectionId")) {

			preparedStatement.setInt(1, WorkflowConstants.STATUS_DRAFT);
			preparedStatement.setInt(2, WorkflowConstants.STATUS_INCOMPLETE);
			preparedStatement.setInt(3, WorkflowConstants.STATUS_SCHEDULED);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					ctCollectionIds.add(resultSet.getLong("ctCollectionId"));
				}
			}
		}

		return ctCollectionIds;
	}

	private String _getExternalReferenceCode(
		String externalReferenceCode, boolean privateLayout) {

		if (!privateLayout) {
			return externalReferenceCode;
		}

		String newExternalReferenceCode =
			externalReferenceCode +
				LayoutConstants.EXTERNAL_REFERENCE_CODE_SUFFIX_PRIVATE;

		if (newExternalReferenceCode.length() <= ModelHintsUtil.getMaxLength(
				Layout.class.getName(), "externalReferenceCode")) {

			return newExternalReferenceCode;
		}

		return PortalUUIDUtil.generate(newExternalReferenceCode.getBytes());
	}

	private Collection<FragmentEntryLinkInfo> _getFragmentEntryLinkInfos(
			long ctCollectionId, long plid, long segmentsExperienceId)
		throws Exception {

		Map<Long, FragmentEntryLinkInfo> fragmentEntryLinkInfos =
			new LinkedHashMap<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select ctCollectionId, externalReferenceCode, ",
					"fragmentEntryLinkId, uuid_ from FragmentEntryLink where ",
					"(ctCollectionId = 0 or ctCollectionId = ?) and plid = ? ",
					"and segmentsExperienceId = ? order by ctCollectionId"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, plid);
			preparedStatement.setLong(3, segmentsExperienceId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long fragmentEntryLinkId = resultSet.getLong(
						"fragmentEntryLinkId");

					fragmentEntryLinkInfos.put(
						fragmentEntryLinkId,
						new FragmentEntryLinkInfo(
							resultSet.getLong("ctCollectionId"),
							resultSet.getString("externalReferenceCode"),
							fragmentEntryLinkId, resultSet.getString("uuid_")));
				}
			}
		}

		return fragmentEntryLinkInfos.values();
	}

	private Collection<LayoutInfo> _getLayoutInfos(
			long ctCollectionId, long groupId, boolean privateLayout)
		throws Exception {

		Map<Long, LayoutInfo> layoutInfos = new LinkedHashMap<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select classNameId, ctCollectionId, ",
					"externalReferenceCode, layoutSetPrototypeLayoutERC, ",
					"plid, system_ from Layout where (ctCollectionId = 0 or ",
					"ctCollectionId = ?) and groupId = ? and privateLayout = ",
					"? order by ctCollectionId"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, groupId);
			preparedStatement.setBoolean(3, privateLayout);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long plid = resultSet.getLong("plid");

					layoutInfos.put(
						plid,
						new LayoutInfo(
							resultSet.getLong("classNameId"),
							resultSet.getLong("ctCollectionId"),
							resultSet.getString("externalReferenceCode"),
							resultSet.getString("layoutSetPrototypeLayoutERC"),
							plid, resultSet.getBoolean("system_")));
				}
			}
		}

		return layoutInfos.values();
	}

	private Collection<LayoutSetInfo> _getLayoutSetInfos(
			long ctCollectionId, long layoutSetPrototypeClassNameId,
			boolean privateLayout)
		throws Exception {

		Map<Long, LayoutSetInfo> layoutSetInfos = new LinkedHashMap<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select Group_.groupId as groupId1, LayoutSet.groupId as ",
					"groupId2, LayoutSet.layoutSetId, LayoutSet.privateLayout ",
					"from LayoutSet inner join LayoutSetPrototype on ",
					"LayoutSetPrototype.companyId = LayoutSet.companyId and ",
					"(LayoutSetPrototype.ctCollectionId = 0 or ",
					"LayoutSetPrototype.ctCollectionId = ?) and ",
					"LayoutSetPrototype.uuid_ = ",
					"LayoutSet.layoutSetPrototypeUuid inner join Group_ on ",
					"Group_.classNameId = ? and Group_.classPK = ",
					"LayoutSetPrototype.layoutSetPrototypeId and ",
					"(Group_.ctCollectionId = 0 or Group_.ctCollectionId = ?) ",
					"where (LayoutSet.ctCollectionId = 0 or ",
					"LayoutSet.ctCollectionId = ?) and ",
					"LayoutSet.layoutSetPrototypeLinkEnabled = ? and ",
					"LayoutSet.privateLayout = ? order by ",
					"LayoutSet.ctCollectionId"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, layoutSetPrototypeClassNameId);
			preparedStatement.setLong(3, ctCollectionId);
			preparedStatement.setLong(4, ctCollectionId);
			preparedStatement.setBoolean(5, true);
			preparedStatement.setBoolean(6, privateLayout);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					layoutSetInfos.put(
						resultSet.getLong("layoutSetId"),
						new LayoutSetInfo(
							resultSet.getLong("groupId2"),
							resultSet.getLong("groupId1"),
							resultSet.getBoolean("privateLayout")));
				}
			}
		}

		return layoutSetInfos.values();
	}

	private Map<Long, String> _getLayoutStructureItemIds(
			long ctCollectionId, long plid, long segmentsExperienceId)
		throws Exception {

		String data = null;

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select LayoutPageTemplateStructureRel.data_ from ",
					"LayoutPageTemplateStructureRel inner join ",
					"LayoutPageTemplateStructure on ",
					"(LayoutPageTemplateStructure.ctCollectionId = 0 or ",
					"LayoutPageTemplateStructure.ctCollectionId = ?) and ",
					"LayoutPageTemplateStructure.",
					"layoutPageTemplateStructureId = ",
					"LayoutPageTemplateStructureRel.",
					"layoutPageTemplateStructureId where ",
					"LayoutPageTemplateStructure.plid = ? and ",
					"(LayoutPageTemplateStructureRel.ctCollectionId = 0 or ",
					"LayoutPageTemplateStructureRel.ctCollectionId = ?) and ",
					"LayoutPageTemplateStructureRel.segmentsExperienceId = ? ",
					"order by LayoutPageTemplateStructureRel.",
					"ctCollectionId"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, plid);
			preparedStatement.setLong(3, ctCollectionId);
			preparedStatement.setLong(4, segmentsExperienceId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					data = resultSet.getString("data_");
				}
			}
		}

		Map<Long, String> layoutStructureItemIds = new HashMap<>();

		if (Validator.isNull(data)) {
			return layoutStructureItemIds;
		}

		LayoutStructure layoutStructure = LayoutStructure.of(data);

		Map<Long, LayoutStructureItem> layoutStructureItems =
			layoutStructure.getFragmentLayoutStructureItems();

		for (Map.Entry<Long, LayoutStructureItem> entry :
				layoutStructureItems.entrySet()) {

			LayoutStructureItem layoutStructureItem = entry.getValue();

			layoutStructureItemIds.put(
				entry.getKey(), layoutStructureItem.getItemId());
		}

		return layoutStructureItemIds;
	}

	private Collection<SegmentsExperienceInfo> _getSegmentsExperienceInfos(
			long ctCollectionId, long plid)
		throws Exception {

		Map<Long, SegmentsExperienceInfo> segmentsExperienceInfos =
			new LinkedHashMap<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select ctCollectionId, externalReferenceCode, ",
					"segmentsExperienceId, segmentsExperienceKey, uuid_ from ",
					"SegmentsExperience where (ctCollectionId = 0 or ",
					"ctCollectionId = ?) and plid = ? order by ",
					"ctCollectionId"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, plid);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long segmentsExperienceId = resultSet.getLong(
						"segmentsExperienceId");

					segmentsExperienceInfos.put(
						segmentsExperienceId,
						new SegmentsExperienceInfo(
							resultSet.getLong("ctCollectionId"),
							resultSet.getString("externalReferenceCode"),
							segmentsExperienceId,
							resultSet.getString("segmentsExperienceKey"),
							resultSet.getString("uuid_")));
				}
			}
		}

		return segmentsExperienceInfos.values();
	}

	private boolean _hasExternalReferenceCode(
			long ctCollectionId, String externalReferenceCode, long groupId,
			long primaryKey, String primaryKeyColumnName, String tableName)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select 1 from ", tableName, " where (ctCollectionId = 0 ",
					"or ctCollectionId = ?) and externalReferenceCode = ? and ",
					"groupId = ? and ", primaryKeyColumnName, " != ?"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setString(2, externalReferenceCode);
			preparedStatement.setLong(3, groupId);
			preparedStatement.setLong(4, primaryKey);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	private boolean _updateExternalReferenceCode(
			long ctCollectionId, String externalReferenceCode, long groupId,
			String newExternalReferenceCode, long primaryKey,
			String primaryKeyColumnName, String tableName)
		throws Exception {

		if (Objects.equals(externalReferenceCode, newExternalReferenceCode)) {
			return true;
		}

		if (_hasExternalReferenceCode(
				ctCollectionId, newExternalReferenceCode, groupId, primaryKey,
				primaryKeyColumnName, tableName)) {

			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to rename ", tableName, " ", primaryKey,
						" to external reference code ",
						newExternalReferenceCode,
						" because it is already in use in group ", groupId));
			}

			return false;
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"update ", tableName, " set externalReferenceCode = ? ",
					"where ctCollectionId = ? and ", primaryKeyColumnName,
					" = ?"))) {

			preparedStatement.setString(1, newExternalReferenceCode);
			preparedStatement.setLong(2, ctCollectionId);
			preparedStatement.setLong(3, primaryKey);

			preparedStatement.executeUpdate();
		}

		return true;
	}

	private void _updateUUID(
			long ctCollectionId, long primaryKey, String primaryKeyColumnName,
			String tableName)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"update ", tableName,
					" set uuid_ = ? where ctCollectionId = ? and ",
					primaryKeyColumnName, " = ?"))) {

			preparedStatement.setString(1, PortalUUIDUtil.generate());
			preparedStatement.setLong(2, ctCollectionId);
			preparedStatement.setLong(3, primaryKey);

			preparedStatement.executeUpdate();
		}
	}

	private void _upgradeFragmentEntryLinks(
			long ctCollectionId, List<Long> ctCollectionIds, long groupId,
			long layoutSetPrototypeLayoutPlid,
			long layoutSetPrototypeSegmentsExperienceId, long plid,
			boolean privateLayout, long segmentsExperienceId)
		throws Exception {

		Map<String, FragmentEntryLinkInfo>
			layoutSetPrototypeFragmentEntryLinkInfos = new HashMap<>();
		Map<String, FragmentEntryLinkInfo>
			layoutSetPrototypeFragmentEntryLinkInfosByUUID = new HashMap<>();

		Map<Long, String> layoutSetPrototypeLayoutStructureItemIds =
			_getLayoutStructureItemIds(
				ctCollectionId, layoutSetPrototypeLayoutPlid,
				layoutSetPrototypeSegmentsExperienceId);

		for (FragmentEntryLinkInfo fragmentEntryLinkInfo :
				_getFragmentEntryLinkInfos(
					ctCollectionId, layoutSetPrototypeLayoutPlid,
					layoutSetPrototypeSegmentsExperienceId)) {

			String layoutStructureItemId =
				layoutSetPrototypeLayoutStructureItemIds.get(
					fragmentEntryLinkInfo._fragmentEntryLinkId);

			if (layoutStructureItemId != null) {
				layoutSetPrototypeFragmentEntryLinkInfos.put(
					layoutStructureItemId, fragmentEntryLinkInfo);
			}

			layoutSetPrototypeFragmentEntryLinkInfosByUUID.put(
				fragmentEntryLinkInfo._uuid, fragmentEntryLinkInfo);
		}

		Map<Long, String> layoutStructureItemIds = _getLayoutStructureItemIds(
			ctCollectionId, plid, segmentsExperienceId);

		for (FragmentEntryLinkInfo fragmentEntryLinkInfo :
				_getFragmentEntryLinkInfos(
					ctCollectionId, plid, segmentsExperienceId)) {

			if (fragmentEntryLinkInfo._ctCollectionId != ctCollectionId) {
				continue;
			}

			FragmentEntryLinkInfo layoutSetPrototypeFragmentEntryLinkInfo =
				layoutSetPrototypeFragmentEntryLinkInfos.get(
					layoutStructureItemIds.get(
						fragmentEntryLinkInfo._fragmentEntryLinkId));

			if (layoutSetPrototypeFragmentEntryLinkInfo == null) {
				layoutSetPrototypeFragmentEntryLinkInfo =
					layoutSetPrototypeFragmentEntryLinkInfosByUUID.get(
						fragmentEntryLinkInfo._uuid);
			}

			if (layoutSetPrototypeFragmentEntryLinkInfo == null) {
				continue;
			}

			if (privateLayout &&
				Objects.equals(
					fragmentEntryLinkInfo._uuid,
					layoutSetPrototypeFragmentEntryLinkInfo._uuid)) {

				_updateUUID(
					ctCollectionId, fragmentEntryLinkInfo._fragmentEntryLinkId,
					FragmentEntryLinkTable.INSTANCE.fragmentEntryLinkId.
						getName(),
					FragmentEntryLinkTable.INSTANCE.getTableName());
			}

			String externalReferenceCode = _getExternalReferenceCode(
				layoutSetPrototypeFragmentEntryLinkInfo._externalReferenceCode,
				privateLayout);

			if (Objects.equals(
					fragmentEntryLinkInfo._externalReferenceCode,
					externalReferenceCode) ||
				!_updateExternalReferenceCode(
					ctCollectionId,
					fragmentEntryLinkInfo._externalReferenceCode, groupId,
					externalReferenceCode,
					fragmentEntryLinkInfo._fragmentEntryLinkId,
					FragmentEntryLinkTable.INSTANCE.fragmentEntryLinkId.
						getName(),
					FragmentEntryLinkTable.INSTANCE.getTableName())) {

				continue;
			}

			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						StringBundler.concat(
							"update FragmentEntryLink set ",
							"originalFragmentEntryLinkERC = ? where ",
							"ctCollectionId in (",
							StringUtil.merge(ctCollectionIds),
							") and groupId = ? and ",
							"originalFragmentEntryLinkERC = ?"))) {

				preparedStatement.setString(1, externalReferenceCode);
				preparedStatement.setLong(2, groupId);
				preparedStatement.setString(
					3, fragmentEntryLinkInfo._externalReferenceCode);

				preparedStatement.executeUpdate();
			}
		}
	}

	private void _upgradeLayout(
			long ctCollectionId, List<Long> ctCollectionIds,
			Map<String, String> externalReferenceCodes, long groupId,
			LayoutInfo layoutInfo, LayoutInfo layoutSetPrototypeLayoutInfo,
			boolean privateLayout)
		throws Exception {

		if (layoutInfo._ctCollectionId == ctCollectionId) {
			String externalReferenceCode = _getExternalReferenceCode(
				layoutSetPrototypeLayoutInfo._externalReferenceCode,
				privateLayout);

			if (!_updateExternalReferenceCode(
					ctCollectionId, layoutInfo._externalReferenceCode, groupId,
					externalReferenceCode, layoutInfo._plid,
					LayoutTable.INSTANCE.plid.getName(),
					LayoutTable.INSTANCE.getTableName())) {

				return;
			}

			if (!Objects.equals(
					layoutInfo._externalReferenceCode, externalReferenceCode)) {

				externalReferenceCodes.put(
					layoutInfo._externalReferenceCode, externalReferenceCode);
			}
		}

		_upgradeSegmentsExperiences(
			ctCollectionId, ctCollectionIds, groupId,
			layoutSetPrototypeLayoutInfo._plid, layoutInfo._plid,
			privateLayout);
	}

	private void _upgradeLayoutSet(
			long ctCollectionId, List<Long> ctCollectionIds,
			long layoutClassNameId, LayoutSetInfo layoutSetInfo)
		throws Exception {

		Map<String, LayoutInfo> layoutSetPrototypeLayoutInfos = new HashMap<>();

		for (LayoutInfo layoutInfo :
				_getLayoutInfos(
					ctCollectionId, layoutSetInfo._layoutSetPrototypeGroupId,
					true)) {

			if ((layoutInfo._classNameId != layoutClassNameId) &&
				layoutInfo._system) {

				continue;
			}

			layoutSetPrototypeLayoutInfos.put(
				layoutInfo._externalReferenceCode, layoutInfo);
		}

		Map<String, String> externalReferenceCodes = new HashMap<>();

		for (LayoutInfo layoutInfo :
				_getLayoutInfos(
					ctCollectionId, layoutSetInfo._groupId,
					layoutSetInfo._privateLayout)) {

			if (Validator.isNull(layoutInfo._layoutSetPrototypeLayoutERC)) {
				continue;
			}

			LayoutInfo layoutSetPrototypeLayoutInfo =
				layoutSetPrototypeLayoutInfos.get(
					layoutInfo._layoutSetPrototypeLayoutERC);

			if (layoutSetPrototypeLayoutInfo == null) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to find the site template page linked to ",
							"page ", layoutInfo._plid, " in group ",
							layoutSetInfo._groupId));
				}

				continue;
			}

			_upgradeLayout(
				ctCollectionId, ctCollectionIds, externalReferenceCodes,
				layoutSetInfo._groupId, layoutInfo,
				layoutSetPrototypeLayoutInfo, layoutSetInfo._privateLayout);
		}

		_upgradeLinkToLayoutLayouts(
			ctCollectionIds, externalReferenceCodes, layoutSetInfo._groupId);
	}

	private void _upgradeLinkToLayoutLayouts(
			List<Long> ctCollectionIds,
			Map<String, String> externalReferenceCodes, long groupId)
		throws Exception {

		if (externalReferenceCodes.isEmpty()) {
			return;
		}

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select ctCollectionId, plid, typeSettings from Layout ",
					"where ctCollectionId in (",
					StringUtil.merge(ctCollectionIds),
					") and groupId = ? and type_ = ?"));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update Layout set typeSettings = ? where ctCollectionId " +
						"= ? and plid = ?")) {

			preparedStatement1.setLong(1, groupId);
			preparedStatement1.setString(
				2, LayoutConstants.TYPE_LINK_TO_LAYOUT);

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					UnicodeProperties unicodeProperties =
						UnicodePropertiesBuilder.fastLoad(
							resultSet.getString("typeSettings")
						).build();

					String externalReferenceCode = externalReferenceCodes.get(
						unicodeProperties.getProperty(
							"linkToLayoutExternalReferenceCode"));

					if (externalReferenceCode == null) {
						continue;
					}

					unicodeProperties.setProperty(
						"linkToLayoutExternalReferenceCode",
						externalReferenceCode);

					preparedStatement2.setString(
						1, unicodeProperties.toString());
					preparedStatement2.setLong(
						2, resultSet.getLong("ctCollectionId"));
					preparedStatement2.setLong(3, resultSet.getLong("plid"));

					preparedStatement2.addBatch();
				}

				preparedStatement2.executeBatch();
			}
		}
	}

	private void _upgradeSegmentsExperiences(
			long ctCollectionId, List<Long> ctCollectionIds, long groupId,
			long layoutSetPrototypeLayoutPlid, long plid, boolean privateLayout)
		throws Exception {

		Map<String, SegmentsExperienceInfo>
			layoutSetPrototypeSegmentsExperienceInfos = new HashMap<>();
		Map<String, SegmentsExperienceInfo>
			layoutSetPrototypeSegmentsExperienceInfosByUUID = new HashMap<>();

		for (SegmentsExperienceInfo segmentsExperienceInfo :
				_getSegmentsExperienceInfos(
					ctCollectionId, layoutSetPrototypeLayoutPlid)) {

			layoutSetPrototypeSegmentsExperienceInfos.put(
				segmentsExperienceInfo._segmentsExperienceKey,
				segmentsExperienceInfo);
			layoutSetPrototypeSegmentsExperienceInfosByUUID.put(
				segmentsExperienceInfo._uuid, segmentsExperienceInfo);
		}

		for (SegmentsExperienceInfo segmentsExperienceInfo :
				_getSegmentsExperienceInfos(ctCollectionId, plid)) {

			SegmentsExperienceInfo layoutSetPrototypeSegmentsExperienceInfo =
				layoutSetPrototypeSegmentsExperienceInfos.get(
					segmentsExperienceInfo._segmentsExperienceKey);

			if (layoutSetPrototypeSegmentsExperienceInfo == null) {
				layoutSetPrototypeSegmentsExperienceInfo =
					layoutSetPrototypeSegmentsExperienceInfosByUUID.get(
						segmentsExperienceInfo._uuid);
			}

			if (layoutSetPrototypeSegmentsExperienceInfo == null) {
				continue;
			}

			if (segmentsExperienceInfo._ctCollectionId == ctCollectionId) {
				if (privateLayout &&
					Objects.equals(
						segmentsExperienceInfo._uuid,
						layoutSetPrototypeSegmentsExperienceInfo._uuid)) {

					_updateUUID(
						ctCollectionId,
						segmentsExperienceInfo._segmentsExperienceId,
						SegmentsExperienceTable.INSTANCE.segmentsExperienceId.
							getName(),
						SegmentsExperienceTable.INSTANCE.getTableName());
				}

				_updateExternalReferenceCode(
					ctCollectionId,
					segmentsExperienceInfo._externalReferenceCode, groupId,
					_getExternalReferenceCode(
						layoutSetPrototypeSegmentsExperienceInfo.
							_externalReferenceCode,
						privateLayout),
					segmentsExperienceInfo._segmentsExperienceId,
					SegmentsExperienceTable.INSTANCE.segmentsExperienceId.
						getName(),
					SegmentsExperienceTable.INSTANCE.getTableName());
			}

			_upgradeFragmentEntryLinks(
				ctCollectionId, ctCollectionIds, groupId,
				layoutSetPrototypeLayoutPlid,
				layoutSetPrototypeSegmentsExperienceInfo._segmentsExperienceId,
				plid, privateLayout,
				segmentsExperienceInfo._segmentsExperienceId);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LayoutUpgradeProcess.class);

	private final ClassNameLocalService _classNameLocalService;

	private static class FragmentEntryLinkInfo {

		private FragmentEntryLinkInfo(
			long ctCollectionId, String externalReferenceCode,
			long fragmentEntryLinkId, String uuid) {

			_ctCollectionId = ctCollectionId;
			_externalReferenceCode = externalReferenceCode;
			_fragmentEntryLinkId = fragmentEntryLinkId;
			_uuid = uuid;
		}

		private final long _ctCollectionId;
		private final String _externalReferenceCode;
		private final long _fragmentEntryLinkId;
		private final String _uuid;

	}

	private static class LayoutInfo {

		private LayoutInfo(
			long classNameId, long ctCollectionId, String externalReferenceCode,
			String layoutSetPrototypeLayoutERC, long plid, boolean system) {

			_classNameId = classNameId;
			_ctCollectionId = ctCollectionId;
			_externalReferenceCode = externalReferenceCode;
			_layoutSetPrototypeLayoutERC = layoutSetPrototypeLayoutERC;
			_plid = plid;
			_system = system;
		}

		private final long _classNameId;
		private final long _ctCollectionId;
		private final String _externalReferenceCode;
		private final String _layoutSetPrototypeLayoutERC;
		private final long _plid;
		private final boolean _system;

	}

	private static class LayoutSetInfo {

		private LayoutSetInfo(
			long groupId, long layoutSetPrototypeGroupId,
			boolean privateLayout) {

			_groupId = groupId;
			_layoutSetPrototypeGroupId = layoutSetPrototypeGroupId;
			_privateLayout = privateLayout;
		}

		private final long _groupId;
		private final long _layoutSetPrototypeGroupId;
		private final boolean _privateLayout;

	}

	private static class SegmentsExperienceInfo {

		private SegmentsExperienceInfo(
			long ctCollectionId, String externalReferenceCode,
			long segmentsExperienceId, String segmentsExperienceKey,
			String uuid) {

			_ctCollectionId = ctCollectionId;
			_externalReferenceCode = externalReferenceCode;
			_segmentsExperienceId = segmentsExperienceId;
			_segmentsExperienceKey = segmentsExperienceKey;
			_uuid = uuid;
		}

		private final long _ctCollectionId;
		private final String _externalReferenceCode;
		private final long _segmentsExperienceId;
		private final String _segmentsExperienceKey;
		private final String _uuid;

	}

}