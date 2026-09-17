/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.entry.type;

import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.model.DepotEntryGroupRel;
import com.liferay.depot.service.DepotEntryGroupRelLocalService;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.fragment.constants.FragmentPortletKeys;
import com.liferay.fragment.item.selector.FragmentEntryItemSelectorCriterion;
import com.liferay.fragment.item.selector.FragmentEntryItemSelectorReturnType;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.model.FragmentEntryVersion;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.item.selector.ItemSelectorCriterion;
import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(
	property = "launch.entry.type.class.name=com.liferay.fragment.model.FragmentEntry",
	service = LaunchEntryType.class
)
public class FragmentEntryLaunchEntryType implements LaunchEntryType {

	@Override
	public Version addPublishedVersion(long classPK, String classVersion)
		throws PortalException {

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.getFragmentEntry(classPK);

		FragmentEntry launchDraftFragmentEntry = _fetchLaunchDraftFragmentEntry(
			fragmentEntry, classVersion);

		if (launchDraftFragmentEntry == null) {
			return _toVersion(
				fragmentEntry,
				_fragmentEntryLocalService.getVersion(
					fragmentEntry, GetterUtil.getInteger(classVersion)));
		}

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		FragmentEntry publishedFragmentEntry =
			_fragmentEntryLocalService.updateFragmentEntry(
				serviceContext.getUserId(), classPK,
				fragmentEntry.getFragmentCollectionId(),
				launchDraftFragmentEntry.getName(),
				launchDraftFragmentEntry.getCss(),
				launchDraftFragmentEntry.getHtml(),
				launchDraftFragmentEntry.getJs(), fragmentEntry.isCacheable(),
				launchDraftFragmentEntry.getConfiguration(),
				fragmentEntry.getIcon(), fragmentEntry.getPreviewFileEntryId(),
				fragmentEntry.isReadOnly(),
				launchDraftFragmentEntry.getTypeOptions(),
				WorkflowConstants.STATUS_APPROVED);

		for (long groupId : _getGroupIds(publishedFragmentEntry)) {
			for (FragmentEntryLink fragmentEntryLink :
					_fragmentEntryLinkLocalService.
						getFragmentEntryLinksByFragmentEntry(
							groupId, publishedFragmentEntry)) {

				_fragmentEntryLinkLocalService.updateLatestChanges(
					publishedFragmentEntry, fragmentEntryLink);
			}
		}

		_fragmentEntryLocalService.deleteFragmentEntry(
			launchDraftFragmentEntry);

		return fetchPublishedVersion(classPK);
	}

	@Override
	public Version addVersion(long classPK, String classVersion)
		throws PortalException {

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.getFragmentEntry(classPK);

		FragmentEntry baseFragmentEntry = _fetchLaunchDraftFragmentEntry(
			fragmentEntry, classVersion);

		if (baseFragmentEntry != null) {
			return _addVersion(
				fragmentEntry, baseFragmentEntry.getName(),
				baseFragmentEntry.getCss(), baseFragmentEntry.getHtml(),
				baseFragmentEntry.getJs(), baseFragmentEntry.getConfiguration(),
				baseFragmentEntry.getTypeOptions());
		}

		FragmentEntryVersion fragmentEntryVersion =
			_fragmentEntryLocalService.getVersion(
				fragmentEntry, GetterUtil.getInteger(classVersion));

		return _addVersion(
			fragmentEntry, fragmentEntryVersion.getName(),
			fragmentEntryVersion.getCss(), fragmentEntryVersion.getHtml(),
			fragmentEntryVersion.getJs(),
			fragmentEntryVersion.getConfiguration(),
			fragmentEntryVersion.getTypeOptions());
	}

	@Override
	public Version fetchPublishedVersion(long classPK) throws PortalException {
		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.getFragmentEntry(classPK);

		FragmentEntryVersion publishedFragmentEntryVersion = null;

		for (FragmentEntryVersion fragmentEntryVersion :
				_fragmentEntryLocalService.getVersions(fragmentEntry)) {

			if ((publishedFragmentEntryVersion == null) ||
				(fragmentEntryVersion.getVersion() >
					publishedFragmentEntryVersion.getVersion())) {

				publishedFragmentEntryVersion = fragmentEntryVersion;
			}
		}

		if (publishedFragmentEntryVersion == null) {
			return null;
		}

		return _toVersion(fragmentEntry, publishedFragmentEntryVersion);
	}

	@Override
	public Version fetchVersion(long classPK, String classVersion) {
		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(classPK);

		if (fragmentEntry == null) {
			return null;
		}

		FragmentEntry launchDraftFragmentEntry = _fetchLaunchDraftFragmentEntry(
			fragmentEntry, classVersion);

		if (launchDraftFragmentEntry != null) {
			return _toVersion(launchDraftFragmentEntry);
		}

		int version = GetterUtil.getInteger(classVersion);

		for (FragmentEntryVersion fragmentEntryVersion :
				_fragmentEntryLocalService.getVersions(fragmentEntry)) {

			if (fragmentEntryVersion.getVersion() == version) {
				return _toVersion(fragmentEntry, fragmentEntryVersion);
			}
		}

		return null;
	}

	@Override
	public long getClassPK(Map<String, String> selectedItemData) {
		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(
				GetterUtil.getLong(selectedItemData.get("groupId")),
				GetterUtil.getString(selectedItemData.get("fragmentEntryKey")));

		if (fragmentEntry == null) {
			return 0;
		}

		return fragmentEntry.getFragmentEntryId();
	}

	@Override
	public ItemSelectorCriterion getItemSelectorCriterion() {
		FragmentEntryItemSelectorCriterion fragmentEntryItemSelectorCriterion =
			new FragmentEntryItemSelectorCriterion();

		fragmentEntryItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			new FragmentEntryItemSelectorReturnType());

		fragmentEntryItemSelectorCriterion.
			setIncludeFragmentCollectionContributors(false);

		return fragmentEntryItemSelectorCriterion;
	}

	@Override
	public String getLabel(Locale locale) {
		return _language.get(locale, "fragment");
	}

	@Override
	public Class<?> getModelClass() {
		return FragmentEntry.class;
	}

	@Override
	public List<Version> getVersions(long classPK, Locale locale)
		throws PortalException {

		List<Version> versions = new ArrayList<>();

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.getFragmentEntry(classPK);

		List<FragmentEntryVersion> fragmentEntryVersions = new ArrayList<>(
			_fragmentEntryLocalService.getVersions(fragmentEntry));

		fragmentEntryVersions.sort(
			Comparator.comparingInt(
				FragmentEntryVersion::getVersion
			).reversed());

		for (FragmentEntryVersion fragmentEntryVersion :
				fragmentEntryVersions) {

			versions.add(_toVersion(fragmentEntry, fragmentEntryVersion));
		}

		for (FragmentEntry launchDraftFragmentEntry :
				_getLaunchDraftFragmentEntries(fragmentEntry)) {

			versions.add(_toVersion(launchDraftFragmentEntry));
		}

		return versions;
	}

	@Override
	public Version publishVersion(long classPK, String classVersion)
		throws PortalException {

		return addPublishedVersion(classPK, classVersion);
	}

	private Version _addVersion(
			FragmentEntry fragmentEntry, String name, String css, String html,
			String js, String configuration, String typeOptions)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		String fragmentEntryKey = _getFragmentEntryKey(fragmentEntry);

		FragmentEntry launchDraftFragmentEntry =
			_fragmentEntryLocalService.addFragmentEntry(
				fragmentEntryKey, serviceContext.getUserId(),
				fragmentEntry.getGroupId(),
				fragmentEntry.getFragmentCollectionId(), fragmentEntryKey, name,
				css, html, js, fragmentEntry.isCacheable(), configuration,
				fragmentEntry.getIcon(), fragmentEntry.getPreviewFileEntryId(),
				false, false, fragmentEntry.getType(), typeOptions,
				WorkflowConstants.STATUS_DRAFT, serviceContext);

		return _toVersion(launchDraftFragmentEntry);
	}

	private FragmentEntry _fetchLaunchDraftFragmentEntry(
		FragmentEntry fragmentEntry, String classVersion) {

		FragmentEntry launchDraftFragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(
				GetterUtil.getLong(classVersion));

		if ((launchDraftFragmentEntry == null) ||
			(launchDraftFragmentEntry.getGroupId() !=
				fragmentEntry.getGroupId()) ||
			!_isLaunchDraftFragmentEntry(
				fragmentEntry, launchDraftFragmentEntry)) {

			return null;
		}

		return launchDraftFragmentEntry;
	}

	private String _getEditURL(
		FragmentEntry fragmentEntry, String redirect,
		RequestBackedPortletURLFactory requestBackedPortletURLFactory) {

		return PortletURLBuilder.create(
			requestBackedPortletURLFactory.createRenderURL(
				FragmentPortletKeys.FRAGMENT)
		).setMVCRenderCommandName(
			"/fragment/edit_fragment_entry"
		).setRedirect(
			redirect
		).setBackURL(
			redirect
		).setParameter(
			"backURLTitle",
			_language.get(LocaleThreadLocal.getThemeDisplayLocale(), "launches")
		).setParameter(
			"fragmentCollectionId", fragmentEntry.getFragmentCollectionId()
		).setParameter(
			"fragmentEntryId", fragmentEntry.getFragmentEntryId()
		).buildString();
	}

	private String _getFragmentEntryKey(FragmentEntry fragmentEntry) {
		for (int i = 1;; i++) {
			String fragmentEntryKey =
				_getFragmentEntryKeyPrefix(fragmentEntry) + i;

			FragmentEntry launchDraftFragmentEntry =
				_fragmentEntryLocalService.fetchFragmentEntry(
					fragmentEntry.getGroupId(), fragmentEntryKey);

			if (launchDraftFragmentEntry == null) {
				return fragmentEntryKey;
			}
		}
	}

	private String _getFragmentEntryKeyPrefix(FragmentEntry fragmentEntry) {
		return fragmentEntry.getFragmentEntryKey() + _FRAGMENT_ENTRY_KEY_INFIX;
	}

	private List<Long> _getGroupIds(FragmentEntry fragmentEntry)
		throws PortalException {

		List<Long> groupIds = ListUtil.fromArray(fragmentEntry.getGroupId());

		Group group = _groupLocalService.getGroup(fragmentEntry.getGroupId());

		if (!group.isDepot()) {
			return groupIds;
		}

		DepotEntry depotEntry = _depotEntryLocalService.fetchGroupDepotEntry(
			group.getGroupId());

		if (depotEntry == null) {
			return groupIds;
		}

		for (DepotEntryGroupRel depotEntryGroupRel :
				_depotEntryGroupRelLocalService.getDepotEntryGroupRels(
					depotEntry)) {

			groupIds.add(depotEntryGroupRel.getToGroupId());
		}

		return groupIds;
	}

	private List<FragmentEntry> _getLaunchDraftFragmentEntries(
		FragmentEntry fragmentEntry) {

		DynamicQuery dynamicQuery = _fragmentEntryLocalService.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("groupId", fragmentEntry.getGroupId()));
		dynamicQuery.add(
			RestrictionsFactoryUtil.like(
				"fragmentEntryKey",
				_getFragmentEntryKeyPrefix(fragmentEntry) +
					StringPool.PERCENT));

		return ListUtil.filter(
			_fragmentEntryLocalService.dynamicQuery(dynamicQuery),
			launchDraftFragmentEntry -> _isLaunchDraftFragmentEntry(
				fragmentEntry, launchDraftFragmentEntry));
	}

	private boolean _isLaunchDraftFragmentEntry(
		FragmentEntry fragmentEntry, FragmentEntry launchDraftFragmentEntry) {

		String fragmentEntryKey =
			launchDraftFragmentEntry.getFragmentEntryKey();

		String fragmentEntryKeyPrefix = _getFragmentEntryKeyPrefix(
			fragmentEntry);

		if (!fragmentEntryKey.startsWith(fragmentEntryKeyPrefix)) {
			return false;
		}

		return Validator.isNumber(
			fragmentEntryKey.substring(fragmentEntryKeyPrefix.length()));
	}

	private Version _toVersion(FragmentEntry launchDraftFragmentEntry) {
		return new Version() {

			@Override
			public String getClassVersion() {
				return String.valueOf(
					launchDraftFragmentEntry.getFragmentEntryId());
			}

			@Override
			public String getEditURL(
				String redirect,
				RequestBackedPortletURLFactory requestBackedPortletURLFactory) {

				return _getEditURL(
					launchDraftFragmentEntry, redirect,
					requestBackedPortletURLFactory);
			}

			@Override
			public long getGroupId() {
				return launchDraftFragmentEntry.getGroupId();
			}

			@Override
			public String getLabel(Locale locale) {
				return _language.get(locale, "draft");
			}

			@Override
			public Date getModifiedDate() {
				return launchDraftFragmentEntry.getModifiedDate();
			}

			@Override
			public BaseModel<?> getPreviewBaseModel() {
				return launchDraftFragmentEntry;
			}

			@Override
			public Serializable getPrimaryKey() {
				return launchDraftFragmentEntry.getFragmentEntryId();
			}

			@Override
			public int getStatus() {
				return launchDraftFragmentEntry.getStatus();
			}

			@Override
			public String getTitle(Locale locale) {
				return launchDraftFragmentEntry.getName();
			}

			@Override
			public String getTypeName(Locale locale) {
				return _language.get(locale, "fragment");
			}

			@Override
			public String getUserName() {
				return launchDraftFragmentEntry.getUserName();
			}

		};
	}

	private Version _toVersion(
		FragmentEntry fragmentEntry,
		FragmentEntryVersion fragmentEntryVersion) {

		return new Version() {

			@Override
			public String getClassVersion() {
				return String.valueOf(fragmentEntryVersion.getVersion());
			}

			@Override
			public String getEditURL(
				String redirect,
				RequestBackedPortletURLFactory requestBackedPortletURLFactory) {

				return _getEditURL(
					fragmentEntry, redirect, requestBackedPortletURLFactory);
			}

			@Override
			public long getGroupId() {
				return fragmentEntry.getGroupId();
			}

			@Override
			public String getLabel(Locale locale) {
				return String.valueOf(fragmentEntryVersion.getVersion());
			}

			@Override
			public Date getModifiedDate() {
				return fragmentEntryVersion.getModifiedDate();
			}

			@Override
			public BaseModel<?> getPreviewBaseModel() {
				FragmentEntry previewFragmentEntry =
					(FragmentEntry)fragmentEntry.clone();

				fragmentEntryVersion.populateVersionedModel(
					previewFragmentEntry);

				return previewFragmentEntry;
			}

			@Override
			public Serializable getPrimaryKey() {
				return fragmentEntry.getFragmentEntryId();
			}

			@Override
			public int getStatus() {
				return fragmentEntryVersion.getStatus();
			}

			@Override
			public String getTitle(Locale locale) {
				return fragmentEntryVersion.getName();
			}

			@Override
			public String getTypeName(Locale locale) {
				return _language.get(locale, "fragment");
			}

			@Override
			public String getUserName() {
				return fragmentEntryVersion.getUserName();
			}

		};
	}

	private static final String _FRAGMENT_ENTRY_KEY_INFIX = "-launch-";

	@Reference
	private DepotEntryGroupRelLocalService _depotEntryGroupRelLocalService;

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Language _language;

}