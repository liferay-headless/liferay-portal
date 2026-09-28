/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.entry.type;

import com.liferay.item.selector.ItemSelectorCriterion;
import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.launch.internal.entry.type.util.LaunchPreviewLayoutUtil;
import com.liferay.layout.constants.LayoutTypeSettingsConstants;
import com.liferay.layout.content.creator.LayoutContentVersionCreator;
import com.liferay.layout.content.model.LayoutContentVersion;
import com.liferay.layout.content.restorer.LayoutContentVersionRestorer;
import com.liferay.layout.content.service.LayoutContentVersionLocalService;
import com.liferay.layout.content.util.comparator.LayoutContentVersionVersionComparator;
import com.liferay.layout.item.selector.LayoutItemSelectorCriterion;
import com.liferay.layout.item.selector.LayoutItemSelectorReturnType;
import com.liferay.layout.util.LayoutServiceContextHelper;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutWrapper;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.preview.PreviewableResolverUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
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
	property = "launch.entry.type.class.name=com.liferay.portal.kernel.model.Layout",
	service = LaunchEntryType.class
)
public class LayoutLaunchEntryType implements LaunchEntryType {

	@Override
	public Version addPublishedVersion(long classPK, String classVersion)
		throws PortalException {

		Layout launchDraftLayout = _fetchLaunchDraftLayout(
			classPK, classVersion);

		if (launchDraftLayout == null) {
			return _getVersion(classPK, classVersion);
		}

		_copyLayoutContent(
			launchDraftLayout, _layoutLocalService.getLayout(classPK));

		Layout draftLayout = _layoutLocalService.fetchDraftLayout(classPK);

		if (draftLayout != null) {
			draftLayout = _copyLayoutContent(launchDraftLayout, draftLayout);

			UnicodeProperties typeSettingsUnicodeProperties =
				draftLayout.getTypeSettingsProperties();

			typeSettingsUnicodeProperties.put(
				LayoutTypeSettingsConstants.KEY_PUBLISHED,
				Boolean.TRUE.toString());

			draftLayout.setStatus(WorkflowConstants.STATUS_APPROVED);

			draftLayout = _layoutLocalService.updateLayout(draftLayout);

			_createLayoutContentVersion(draftLayout);
		}

		_layoutLocalService.deleteLayout(launchDraftLayout);

		return fetchPublishedVersion(classPK);
	}

	@Override
	public Version addVersion(long classPK, String classVersion)
		throws PortalException {

		Layout layout = _layoutLocalService.getLayout(classPK);

		if (!layout.isTypeContent()) {
			throw new PortalException(
				"Layout " + classPK + " is not a content page");
		}

		Layout baseLayout = _fetchLayout(classPK, classVersion);

		if (baseLayout != null) {
			return _toVersion(
				_copyLayoutContent(
					baseLayout,
					_addLaunchLayout(
						layout, baseLayout,
						_getLaunchDraftExternalReferenceCode(layout))));
		}

		LayoutContentVersion layoutContentVersion = _fetchLayoutContentVersion(
			layout, classVersion);

		if (layoutContentVersion == null) {
			throw new PortalException(
				StringBundler.concat(
					"Version ", classVersion,
					" is not a version of the layout ", classPK));
		}

		Layout launchDraftLayout = _copyLayoutContent(
			layout,
			_addLaunchLayout(
				layout, layout, _getLaunchDraftExternalReferenceCode(layout)));

		if (layoutContentVersion.getLayoutContentVersionId() ==
				_getLatestApprovedLayoutContentVersionId(layout)) {

			return _toVersion(launchDraftLayout);
		}

		return _toVersion(
			_restoreLayoutContentVersion(
				launchDraftLayout, layoutContentVersion));
	}

	@Override
	public Version fetchPublishedVersion(long classPK) throws PortalException {
		Layout layout = _layoutLocalService.getLayout(classPK);

		LayoutContentVersion layoutContentVersion =
			_fetchLatestApprovedLayoutContentVersion(layout);

		if (layoutContentVersion != null) {
			return _toVersion(layout, layoutContentVersion);
		}

		return _toVersion(layout);
	}

	@Override
	public Version fetchVersion(long classPK, String classVersion)
		throws PortalException {

		Layout layout = _fetchLayout(classPK, classVersion);

		if (layout != null) {
			return _toVersion(layout);
		}

		layout = _layoutLocalService.fetchLayout(classPK);

		if (layout == null) {
			return null;
		}

		LayoutContentVersion layoutContentVersion = _fetchLayoutContentVersion(
			layout, classVersion);

		if (layoutContentVersion == null) {
			return null;
		}

		return _toVersion(layout, layoutContentVersion);
	}

	@Override
	public long getClassPK(Map<String, String> selectedItemData) {
		Layout layout = _layoutLocalService.fetchLayout(
			GetterUtil.getLong(selectedItemData.get("plid")));

		if ((layout == null) || !layout.isTypeContent() ||
			layout.isDraftLayout()) {

			return 0;
		}

		return layout.getPlid();
	}

	@Override
	public ItemSelectorCriterion getItemSelectorCriterion() {
		LayoutItemSelectorCriterion layoutItemSelectorCriterion =
			new LayoutItemSelectorCriterion();

		layoutItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			new LayoutItemSelectorReturnType());

		return layoutItemSelectorCriterion;
	}

	@Override
	public String getLabel(Locale locale) {
		return _language.get(locale, "page");
	}

	@Override
	public Class<?> getModelClass() {
		return Layout.class;
	}

	@Override
	public List<Version> getVersions(long classPK, Locale locale)
		throws PortalException {

		List<Version> versions = new ArrayList<>();

		Layout layout = _layoutLocalService.getLayout(classPK);

		List<LayoutContentVersion> layoutContentVersions =
			_getLayoutContentVersions(layout);

		if (layoutContentVersions.isEmpty()) {
			versions.add(_toVersion(layout));
		}
		else {
			for (LayoutContentVersion layoutContentVersion :
					layoutContentVersions) {

				versions.add(_toVersion(layout, layoutContentVersion));
			}
		}

		if (!layout.isPublished()) {
			Layout draftLayout = _layoutLocalService.fetchDraftLayout(classPK);

			if (draftLayout != null) {
				versions.add(_toVersion(draftLayout));
			}
		}

		for (Layout launchDraftLayout : _getLaunchDraftLayouts(classPK)) {
			versions.add(_toVersion(launchDraftLayout));
		}

		return versions;
	}

	@Override
	public Version publishVersion(long classPK, String classVersion)
		throws PortalException {

		return addPublishedVersion(classPK, classVersion);
	}

	private Layout _addLaunchLayout(
			Layout layout, Layout baseLayout, String externalReferenceCode)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		return _layoutLocalService.addLayout(
			externalReferenceCode, serviceContext.getUserId(),
			layout.getGroupId(), layout.isPrivateLayout(),
			layout.getParentLayoutId(), _portal.getClassNameId(Layout.class),
			layout.getPlid(), baseLayout.getNameMap(), baseLayout.getTitleMap(),
			baseLayout.getDescriptionMap(), baseLayout.getKeywordsMap(),
			baseLayout.getRobotsMap(), layout.getType(),
			baseLayout.getTypeSettings(), true, true, Collections.emptyMap(),
			baseLayout.getMasterLayoutPageTemplateEntryERC(), serviceContext);
	}

	private Layout _copyLayoutContent(Layout sourceLayout, Layout targetLayout)
		throws PortalException {

		try {
			targetLayout = _layoutLocalService.copyLayoutContent(
				sourceLayout, targetLayout);
		}
		catch (Exception exception) {
			throw new PortalException(exception);
		}

		targetLayout.setNameMap(sourceLayout.getNameMap());
		targetLayout.setTitleMap(sourceLayout.getTitleMap());
		targetLayout.setDescriptionMap(sourceLayout.getDescriptionMap());
		targetLayout.setKeywordsMap(sourceLayout.getKeywordsMap());
		targetLayout.setRobotsMap(sourceLayout.getRobotsMap());

		return _layoutLocalService.updateLayout(targetLayout);
	}

	private void _createLayoutContentVersion(Layout layout) {
		LayoutContentVersionCreator layoutContentVersionCreator =
			_layoutContentVersionCreatorSnapshot.get();

		if (layoutContentVersionCreator == null) {
			return;
		}

		layoutContentVersionCreator.createLayoutContentVersion(layout);
	}

	private LayoutContentVersion _fetchLatestApprovedLayoutContentVersion(
		Layout layout) {

		LayoutContentVersionLocalService layoutContentVersionLocalService =
			_fetchLayoutContentVersionLocalService(layout);

		if (layoutContentVersionLocalService == null) {
			return null;
		}

		long layoutContentVersionId = _getLatestApprovedLayoutContentVersionId(
			layout);

		if (layoutContentVersionId == 0) {
			return null;
		}

		return layoutContentVersionLocalService.fetchLayoutContentVersion(
			layoutContentVersionId);
	}

	private Layout _fetchLaunchDraftLayout(long classPK, String classVersion) {
		Layout layout = _fetchLayout(classPK, classVersion);

		if ((layout == null) || !_isLaunchDraftLayout(layout)) {
			return null;
		}

		return layout;
	}

	private Layout _fetchLayout(long classPK, String classVersion) {
		Layout layout = _layoutLocalService.fetchLayout(
			GetterUtil.getLong(classVersion));

		if ((layout == null) ||
			LaunchPreviewLayoutUtil.isPreviewLayout(layout)) {

			return null;
		}

		if ((layout.getPlid() != classPK) &&
			(!layout.isDraftLayout() || (layout.getClassPK() != classPK))) {

			return null;
		}

		return layout;
	}

	private LayoutContentVersion _fetchLayoutContentVersion(
		Layout layout, String classVersion) {

		LayoutContentVersionLocalService layoutContentVersionLocalService =
			_fetchLayoutContentVersionLocalService(layout);

		if (layoutContentVersionLocalService == null) {
			return null;
		}

		LayoutContentVersion layoutContentVersion =
			layoutContentVersionLocalService.
				fetchLayoutContentVersionByExternalReferenceCode(
					classVersion, layout.getGroupId());

		if (layoutContentVersion == null) {
			layoutContentVersion =
				layoutContentVersionLocalService.fetchLayoutContentVersion(
					GetterUtil.getLong(classVersion));
		}

		Layout draftLayout = _layoutLocalService.fetchDraftLayout(
			layout.getPlid());

		if ((layoutContentVersion == null) || (draftLayout == null) ||
			(layoutContentVersion.getPlid() != draftLayout.getPlid())) {

			return null;
		}

		return layoutContentVersion;
	}

	/**
	 * Returns the service that keeps the page's version history, or
	 * <code>null</code> when the feature is unavailable. A page that is not
	 * versioned still has a published version and a current draft to branch
	 * from, so every caller falls back to the layouts themselves.
	 */
	private LayoutContentVersionLocalService
		_fetchLayoutContentVersionLocalService(Layout layout) {

		if (!FeatureFlagManagerUtil.isEnabled(
				layout.getCompanyId(), "LPD-10622")) {

			return null;
		}

		return _layoutContentVersionLocalServiceSnapshot.get();
	}

	private String _getClassVersion(LayoutContentVersion layoutContentVersion) {
		String externalReferenceCode =
			layoutContentVersion.getExternalReferenceCode();

		if (Validator.isNotNull(externalReferenceCode)) {
			return externalReferenceCode;
		}

		return String.valueOf(layoutContentVersion.getLayoutContentVersionId());
	}

	private String _getEditURL(Layout layout, String redirect)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

		return HttpComponentsUtil.addParameters(
			_portal.getLayoutFullURL(layout, themeDisplay), "p_l_back_url",
			redirect, "p_l_back_url_title",
			_language.get(themeDisplay.getLocale(), "launches"), "p_l_mode",
			Constants.EDIT);
	}

	private String _getLabel(Layout layout, Locale locale) {
		if (!layout.isDraftLayout()) {
			return _language.get(locale, "published");
		}

		if (_isLaunchDraftLayout(layout)) {
			return _language.get(locale, "draft");
		}

		return _language.get(locale, "current-draft");
	}

	private long _getLatestApprovedLayoutContentVersionId(Layout layout) {
		LayoutContentVersionLocalService layoutContentVersionLocalService =
			_fetchLayoutContentVersionLocalService(layout);

		Layout draftLayout = _layoutLocalService.fetchDraftLayout(
			layout.getPlid());

		if ((layoutContentVersionLocalService == null) ||
			(draftLayout == null)) {

			return 0;
		}

		return layoutContentVersionLocalService.
			getLatestApprovedLayoutContentVersionId(draftLayout.getPlid());
	}

	private String _getLaunchDraftExternalReferenceCode(Layout layout) {
		for (int i = 1;; i++) {
			String externalReferenceCode = StringBundler.concat(
				layout.getExternalReferenceCode(),
				LayoutConstants.EXTERNAL_REFERENCE_CODE_INFIX_LAUNCH_DRAFT, i);

			Layout launchDraftLayout =
				_layoutLocalService.fetchLayoutByExternalReferenceCode(
					externalReferenceCode, layout.getGroupId());

			if (launchDraftLayout == null) {
				return externalReferenceCode;
			}
		}
	}

	private List<Layout> _getLaunchDraftLayouts(long classPK) {
		DynamicQuery dynamicQuery = _layoutLocalService.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"classNameId", _portal.getClassNameId(Layout.class)));
		dynamicQuery.add(RestrictionsFactoryUtil.eq("classPK", classPK));
		dynamicQuery.add(
			RestrictionsFactoryUtil.like(
				"externalReferenceCode",
				StringBundler.concat(
					StringPool.PERCENT,
					LayoutConstants.EXTERNAL_REFERENCE_CODE_INFIX_LAUNCH_DRAFT,
					StringPool.PERCENT)));
		dynamicQuery.add(
			RestrictionsFactoryUtil.not(
				RestrictionsFactoryUtil.like(
					"externalReferenceCode",
					StringBundler.concat(
						StringPool.PERCENT,
						LaunchPreviewLayoutUtil.EXTERNAL_REFERENCE_CODE_INFIX,
						StringPool.PERCENT))));

		dynamicQuery.addOrder(OrderFactoryUtil.desc("plid"));

		return _layoutLocalService.dynamicQuery(dynamicQuery);
	}

	private List<LayoutContentVersion> _getLayoutContentVersions(Layout layout)
		throws PortalException {

		LayoutContentVersionLocalService layoutContentVersionLocalService =
			_fetchLayoutContentVersionLocalService(layout);

		Layout draftLayout = _layoutLocalService.fetchDraftLayout(
			layout.getPlid());

		if ((layoutContentVersionLocalService == null) ||
			(draftLayout == null)) {

			return Collections.emptyList();
		}

		return ListUtil.sort(
			layoutContentVersionLocalService.getLayoutContentVersions(
				draftLayout.getPlid()),
			LayoutContentVersionVersionComparator.getInstance(false));
	}

	/**
	 * Materializes the version as a layout of its own the first time it is
	 * previewed, and hands that same layout to every preview after it. A
	 * version holds the page as data, while rendering resolves a page's
	 * structure and its fragment entry links by PLID, so the only version that
	 * a page can render on its own is the one that is live. Returns
	 * <code>null</code> when the version cannot be materialized, which leaves
	 * the preview on the live page rather than failing the request it is
	 * rendering.
	 */
	private Layout _getPreviewLayout(
		Layout layout, LayoutContentVersion layoutContentVersion) {

		String externalReferenceCode =
			LaunchPreviewLayoutUtil.getExternalReferenceCode(
				layout, layoutContentVersion);

		Layout previewLayout =
			_layoutLocalService.fetchLayoutByExternalReferenceCode(
				externalReferenceCode, layout.getGroupId());

		if (previewLayout != null) {
			return previewLayout;
		}

		User user = null;

		try {
			user = _userLocalService.getUser(layout.getUserId());
		}
		catch (PortalException portalException) {
			_log.error(
				"Unable to materialize version " +
					_getClassVersion(layoutContentVersion),
				portalException);

			return null;
		}

		try (SafeCloseable safeCloseable =
				PreviewableResolverUtil.setPreviewIdWithSafeCloseable(null);

			AutoCloseable autoCloseable =
				_layoutServiceContextHelper.getServiceContextAutoCloseable(
					layout, user)) {

			return _restoreLayoutContentVersion(
				_copyLayoutContent(
					layout,
					_addLaunchLayout(layout, layout, externalReferenceCode)),
				layoutContentVersion);
		}
		catch (Exception exception) {
			previewLayout =
				_layoutLocalService.fetchLayoutByExternalReferenceCode(
					externalReferenceCode, layout.getGroupId());

			if (previewLayout != null) {
				return previewLayout;
			}

			_log.error(
				"Unable to materialize version " +
					_getClassVersion(layoutContentVersion),
				exception);

			return null;
		}
	}

	private Version _getVersion(long classPK, String classVersion)
		throws PortalException {

		Version version = fetchVersion(classPK, classVersion);

		if (version == null) {
			throw new PortalException(
				StringBundler.concat(
					"Version ", classVersion,
					" is not a version of the layout ", classPK));
		}

		return version;
	}

	private boolean _isLaunchDraftLayout(Layout layout) {
		if (LaunchPreviewLayoutUtil.isPreviewLayout(layout)) {
			return false;
		}

		String externalReferenceCode = layout.getExternalReferenceCode();

		return externalReferenceCode.contains(
			LayoutConstants.EXTERNAL_REFERENCE_CODE_INFIX_LAUNCH_DRAFT);
	}

	private Layout _restoreLayoutContentVersion(
			Layout launchDraftLayout, LayoutContentVersion layoutContentVersion)
		throws PortalException {

		LayoutContentVersionRestorer layoutContentVersionRestorer =
			_layoutContentVersionRestorerSnapshot.get();

		if (layoutContentVersionRestorer == null) {
			throw new PortalException(
				"Unable to branch from version " +
					_getClassVersion(layoutContentVersion));
		}

		try {
			return layoutContentVersionRestorer.restoreLayoutContentVersion(
				launchDraftLayout, layoutContentVersion,
				ServiceContextThreadLocal.getServiceContext());
		}
		catch (Exception exception) {
			throw new PortalException(exception);
		}
	}

	private Version _toVersion(Layout layout) {
		return new Version() {

			@Override
			public String getClassVersion() {
				return String.valueOf(layout.getPlid());
			}

			@Override
			public String getEditURL(
					String redirect,
					RequestBackedPortletURLFactory
						requestBackedPortletURLFactory)
				throws PortalException {

				return _getEditURL(layout, redirect);
			}

			@Override
			public long getGroupId() {
				return layout.getGroupId();
			}

			@Override
			public String getLabel(Locale locale) {
				return _getLabel(layout, locale);
			}

			@Override
			public Date getModifiedDate() {
				return layout.getModifiedDate();
			}

			@Override
			public BaseModel<?> getPreviewBaseModel() {
				if (!_isLaunchDraftLayout(layout)) {
					return layout;
				}

				return new LaunchLayoutWrapper(layout);
			}

			@Override
			public Serializable getPrimaryKey() {
				return layout.getPlid();
			}

			@Override
			public int getStatus() {
				if (layout.isDraftLayout()) {
					return WorkflowConstants.STATUS_DRAFT;
				}

				return layout.getStatus();
			}

			@Override
			public String getTitle(Locale locale) {
				return layout.getName(locale);
			}

			@Override
			public String getTypeName(Locale locale) {
				return _language.get(locale, "content-page");
			}

			@Override
			public String getUserName() {
				return layout.getUserName();
			}

		};
	}

	/**
	 * Reports the page itself as the model to preview, because a version holds
	 * the page's content rather than a page of its own. Only the version that
	 * is live can be rendered, which is the one a launch that has not been
	 * superseded yet points at.
	 */
	private Version _toVersion(
		Layout layout, LayoutContentVersion layoutContentVersion) {

		return new Version() {

			@Override
			public String getClassVersion() {
				return _getClassVersion(layoutContentVersion);
			}

			@Override
			public String getEditURL(
					String redirect,
					RequestBackedPortletURLFactory
						requestBackedPortletURLFactory)
				throws PortalException {

				return _getEditURL(layout, redirect);
			}

			@Override
			public long getGroupId() {
				return layoutContentVersion.getGroupId();
			}

			@Override
			public String getLabel(Locale locale) {
				return String.valueOf(layoutContentVersion.getVersion());
			}

			@Override
			public Date getModifiedDate() {
				return layoutContentVersion.getModifiedDate();
			}

			@Override
			public BaseModel<?> getPreviewBaseModel() {
				if (layoutContentVersion.getLayoutContentVersionId() ==
						_getLatestApprovedLayoutContentVersionId(layout)) {

					return layout;
				}

				Layout previewLayout = _getPreviewLayout(
					layout, layoutContentVersion);

				if (previewLayout == null) {
					return layout;
				}

				return new LaunchLayoutWrapper(previewLayout);
			}

			@Override
			public Serializable getPrimaryKey() {
				return layout.getPlid();
			}

			@Override
			public int getStatus() {
				return layoutContentVersion.getStatus();
			}

			@Override
			public String getTitle(Locale locale) {
				return layoutContentVersion.getName(locale);
			}

			@Override
			public String getTypeName(Locale locale) {
				return _language.get(locale, "content-page");
			}

			@Override
			public String getUserName() {
				return layoutContentVersion.getUserName();
			}

		};
	}

	/**
	 * Reports the friendly URL of the layout the launch copies so that the
	 * preview stays on the live page's URL. Without this, the friendly URL
	 * servlet compares the requested URL against the copy's own friendly URL
	 * and redirects the browser to it, stranding the user on a layout that
	 * renders the copy no matter which launch is being previewed.
	 */
	private class LaunchLayoutWrapper extends LayoutWrapper {

		public LaunchLayoutWrapper(Layout launchDraftLayout) {
			super(launchDraftLayout);

			Layout layout = _layoutLocalService.fetchLayout(
				launchDraftLayout.getClassPK());

			if (layout == null) {
				layout = launchDraftLayout;
			}

			_layout = layout;
		}

		@Override
		public String getFriendlyURL() {
			return _layout.getFriendlyURL();
		}

		@Override
		public String getFriendlyURL(Locale locale) {
			return _layout.getFriendlyURL(locale);
		}

		@Override
		public Map<Locale, String> getFriendlyURLMap() {
			return _layout.getFriendlyURLMap();
		}

		@Override
		public String getFriendlyURLsXML() {
			return _layout.getFriendlyURLsXML();
		}

		private final Layout _layout;

	}

	private static final Snapshot<LayoutContentVersionCreator>
		_layoutContentVersionCreatorSnapshot = new Snapshot<>(
			LayoutLaunchEntryType.class, LayoutContentVersionCreator.class,
			null, true);
	private static final Snapshot<LayoutContentVersionLocalService>
		_layoutContentVersionLocalServiceSnapshot = new Snapshot<>(
			LayoutLaunchEntryType.class, LayoutContentVersionLocalService.class,
			null, true);
	private static final Snapshot<LayoutContentVersionRestorer>
		_layoutContentVersionRestorerSnapshot = new Snapshot<>(
			LayoutLaunchEntryType.class, LayoutContentVersionRestorer.class,
			null, true);

	@Reference
	private Language _language;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutServiceContextHelper _layoutServiceContextHelper;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

	private static final Log _log = LogFactoryUtil.getLog(
		LayoutLaunchEntryType.class);

}