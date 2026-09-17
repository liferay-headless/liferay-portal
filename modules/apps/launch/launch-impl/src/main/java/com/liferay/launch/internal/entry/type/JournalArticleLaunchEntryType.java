/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.entry.type;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.item.selector.ItemSelectorCriterion;
import com.liferay.item.selector.criteria.InfoItemItemSelectorReturnType;
import com.liferay.item.selector.criteria.info.item.criterion.InfoItemItemSelectorCriterion;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.exception.NoSuchArticleException;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalArticleResource;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.service.JournalArticleResourceLocalService;
import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
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
	property = "launch.entry.type.class.name=com.liferay.journal.model.JournalArticle",
	service = LaunchEntryType.class
)
public class JournalArticleLaunchEntryType implements LaunchEntryType {

	@Override
	public Version addPublishedVersion(long classPK, String classVersion)
		throws PortalException {

		JournalArticle launchDraftJournalArticle =
			_fetchLaunchDraftJournalArticle(classPK, classVersion);

		if (launchDraftJournalArticle == null) {
			return _toVersion(_getJournalArticle(classPK, classVersion));
		}

		JournalArticle journalArticle =
			_journalArticleLocalService.getLatestArticle(
				classPK, WorkflowConstants.STATUS_APPROVED);

		ServiceContext serviceContext = _getServiceContext(
			WorkflowConstants.ACTION_PUBLISH);

		JournalArticle publishedJournalArticle =
			_journalArticleLocalService.updateArticle(
				serviceContext.getUserId(), journalArticle.getGroupId(),
				journalArticle.getFolderId(), journalArticle.getArticleId(),
				journalArticle.getVersion(),
				launchDraftJournalArticle.getTitleMap(),
				launchDraftJournalArticle.getDescriptionMap(),
				launchDraftJournalArticle.getContent(),
				journalArticle.getLayoutUuid(), serviceContext);

		_journalArticleLocalService.deleteArticle(
			launchDraftJournalArticle, null, serviceContext);

		return _toVersion(publishedJournalArticle);
	}

	@Override
	public Version addVersion(long classPK, String classVersion)
		throws PortalException {

		JournalArticle journalArticle =
			_journalArticleLocalService.getLatestArticle(
				classPK, WorkflowConstants.STATUS_APPROVED);

		JournalArticle baseJournalArticle = _fetchLaunchDraftJournalArticle(
			classPK, classVersion);

		if (baseJournalArticle == null) {
			baseJournalArticle = _getJournalArticle(classPK, classVersion);
		}

		ServiceContext serviceContext = _getServiceContext(
			WorkflowConstants.ACTION_SAVE_DRAFT);

		return _toLaunchDraftVersion(
			_journalArticleLocalService.addArticle(
				_getExternalReferenceCode(classPK, journalArticle.getGroupId()),
				serviceContext.getUserId(), journalArticle.getGroupId(),
				journalArticle.getFolderId(), baseJournalArticle.getTitleMap(),
				baseJournalArticle.getDescriptionMap(),
				baseJournalArticle.getContent(),
				journalArticle.getDDMStructureId(),
				journalArticle.getDDMTemplateKey(), serviceContext));
	}

	@Override
	public Version fetchPublishedVersion(long classPK) {
		JournalArticle journalArticle =
			_journalArticleLocalService.fetchLatestArticle(
				classPK, WorkflowConstants.STATUS_APPROVED);

		if (journalArticle == null) {
			return null;
		}

		return _toVersion(journalArticle);
	}

	@Override
	public Version fetchVersion(long classPK, String classVersion) {
		JournalArticle launchDraftJournalArticle =
			_fetchLaunchDraftJournalArticle(classPK, classVersion);

		if (launchDraftJournalArticle != null) {
			return _toLaunchDraftVersion(launchDraftJournalArticle);
		}

		JournalArticle journalArticle = _fetchJournalArticle(
			classPK, classVersion);

		if (journalArticle == null) {
			return null;
		}

		return _toVersion(journalArticle);
	}

	@Override
	public long getClassPK(Map<String, String> selectedItemData) {
		return GetterUtil.getLong(selectedItemData.get("classPK"));
	}

	@Override
	public ItemSelectorCriterion getItemSelectorCriterion() {
		InfoItemItemSelectorCriterion infoItemItemSelectorCriterion =
			new InfoItemItemSelectorCriterion();

		infoItemItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			new InfoItemItemSelectorReturnType());
		infoItemItemSelectorCriterion.setItemType(
			JournalArticle.class.getName());

		return infoItemItemSelectorCriterion;
	}

	@Override
	public String getLabel(Locale locale) {
		return _language.get(locale, "web-content");
	}

	@Override
	public Class<?> getModelClass() {
		return JournalArticle.class;
	}

	@Override
	public List<Version> getVersions(long classPK, Locale locale)
		throws PortalException {

		List<Version> versions = new ArrayList<>();

		for (JournalArticle journalArticle :
				_journalArticleLocalService.getArticlesByResourcePrimKey(
					classPK)) {

			versions.add(_toVersion(journalArticle));
		}

		versions.sort(
			Comparator.comparingDouble(
				(Version version) -> GetterUtil.getDouble(
					version.getClassVersion())
			).reversed());

		for (JournalArticle launchDraftJournalArticle :
				_getLaunchDraftJournalArticles(classPK)) {

			versions.add(_toLaunchDraftVersion(launchDraftJournalArticle));
		}

		return versions;
	}

	@Override
	public Version publishVersion(long classPK, String classVersion)
		throws PortalException {

		return addPublishedVersion(classPK, classVersion);
	}

	private JournalArticle _fetchJournalArticle(
		long classPK, String classVersion) {

		JournalArticleResource journalArticleResource =
			_journalArticleResourceLocalService.fetchJournalArticleResource(
				classPK);

		if (journalArticleResource == null) {
			return null;
		}

		return _journalArticleLocalService.fetchArticle(
			journalArticleResource.getGroupId(),
			journalArticleResource.getArticleId(),
			GetterUtil.getDouble(classVersion));
	}

	private JournalArticle _fetchLaunchDraftJournalArticle(
		long classPK, String classVersion) {

		JournalArticle launchDraftJournalArticle =
			_journalArticleLocalService.fetchLatestArticle(
				GetterUtil.getLong(classVersion), WorkflowConstants.STATUS_ANY,
				false);

		if ((launchDraftJournalArticle == null) ||
			!_isLaunchDraftJournalArticle(classPK, launchDraftJournalArticle)) {

			return null;
		}

		return launchDraftJournalArticle;
	}

	private String _getExternalReferenceCode(long classPK, long groupId) {
		for (int i = 1;; i++) {
			String externalReferenceCode =
				_getExternalReferenceCodePrefix(classPK) + i;

			JournalArticle launchDraftJournalArticle =
				_journalArticleLocalService.
					fetchLatestArticleByExternalReferenceCode(
						groupId, externalReferenceCode);

			if (launchDraftJournalArticle == null) {
				return externalReferenceCode;
			}
		}
	}

	private String _getExternalReferenceCodePrefix(long classPK) {
		return classPK + _EXTERNAL_REFERENCE_CODE_INFIX_LAUNCH_DRAFT;
	}

	private JournalArticle _getJournalArticle(long classPK, String classVersion)
		throws PortalException {

		JournalArticle journalArticle = _fetchJournalArticle(
			classPK, classVersion);

		if (journalArticle == null) {
			throw new NoSuchArticleException(
				StringBundler.concat(
					"No JournalArticle exists with the key {resourcePrimKey=",
					classPK, ", version=", classVersion, "}"));
		}

		return journalArticle;
	}

	private List<JournalArticle> _getLaunchDraftJournalArticles(long classPK)
		throws PortalException {

		List<JournalArticle> launchDraftJournalArticles = new ArrayList<>();

		JournalArticle journalArticle =
			_journalArticleLocalService.getLatestArticle(
				classPK, WorkflowConstants.STATUS_APPROVED);

		for (int i = 1;; i++) {
			JournalArticle launchDraftJournalArticle =
				_journalArticleLocalService.
					fetchLatestArticleByExternalReferenceCode(
						journalArticle.getGroupId(),
						_getExternalReferenceCodePrefix(classPK) + i);

			if (launchDraftJournalArticle == null) {
				return launchDraftJournalArticles;
			}

			launchDraftJournalArticles.add(launchDraftJournalArticle);
		}
	}

	private ServiceContext _getServiceContext(int workflowAction) {
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		serviceContext = (ServiceContext)serviceContext.clone();

		serviceContext.setWorkflowAction(workflowAction);

		return serviceContext;
	}

	private boolean _isLaunchDraftJournalArticle(
		long classPK, JournalArticle launchDraftJournalArticle) {

		String externalReferenceCode =
			launchDraftJournalArticle.getExternalReferenceCode();

		String externalReferenceCodePrefix = _getExternalReferenceCodePrefix(
			classPK);

		if (!externalReferenceCode.startsWith(externalReferenceCodePrefix)) {
			return false;
		}

		return Validator.isNumber(
			externalReferenceCode.substring(
				externalReferenceCodePrefix.length()));
	}

	private Version _toLaunchDraftVersion(
		JournalArticle launchDraftJournalArticle) {

		Version version = _toVersion(launchDraftJournalArticle);

		return new Version() {

			@Override
			public String getClassVersion() {
				return String.valueOf(
					launchDraftJournalArticle.getResourcePrimKey());
			}

			@Override
			public String getEditURL(
					String redirect,
					RequestBackedPortletURLFactory
						requestBackedPortletURLFactory)
				throws PortalException {

				return version.getEditURL(
					redirect, requestBackedPortletURLFactory);
			}

			@Override
			public long getGroupId() {
				return version.getGroupId();
			}

			@Override
			public String getLabel(Locale locale) {
				return _language.get(locale, "draft");
			}

			@Override
			public Date getModifiedDate() {
				return version.getModifiedDate();
			}

			@Override
			public BaseModel<?> getPreviewBaseModel() throws PortalException {
				return version.getPreviewBaseModel();
			}

			@Override
			public Serializable getPrimaryKey() {
				return version.getPrimaryKey();
			}

			@Override
			public int getStatus() {
				return version.getStatus();
			}

			@Override
			public String getTitle(Locale locale) {
				return version.getTitle(locale);
			}

			@Override
			public String getTypeName(Locale locale) throws PortalException {
				return version.getTypeName(locale);
			}

			@Override
			public String getUserName() {
				return version.getUserName();
			}

		};
	}

	private Version _toVersion(JournalArticle journalArticle) {
		return new Version() {

			@Override
			public String getClassVersion() {
				return String.valueOf(journalArticle.getVersion());
			}

			@Override
			public String getEditURL(
				String redirect,
				RequestBackedPortletURLFactory requestBackedPortletURLFactory) {

				return PortletURLBuilder.create(
					requestBackedPortletURLFactory.createRenderURL(
						JournalPortletKeys.JOURNAL)
				).setMVCRenderCommandName(
					"/journal/edit_article"
				).setRedirect(
					redirect
				).setBackURL(
					redirect
				).setParameter(
					"articleId", journalArticle.getArticleId()
				).setParameter(
					"backURLTitle",
					_language.get(
						LocaleThreadLocal.getThemeDisplayLocale(), "launches")
				).setParameter(
					"folderId", journalArticle.getFolderId()
				).setParameter(
					"groupId", journalArticle.getGroupId()
				).setParameter(
					"version", journalArticle.getVersion()
				).buildString();
			}

			@Override
			public long getGroupId() {
				return journalArticle.getGroupId();
			}

			@Override
			public String getLabel(Locale locale) {
				return String.valueOf(journalArticle.getVersion());
			}

			@Override
			public Date getModifiedDate() {
				return journalArticle.getModifiedDate();
			}

			@Override
			public BaseModel<?> getPreviewBaseModel() {
				return journalArticle;
			}

			@Override
			public Serializable getPrimaryKey() {
				return journalArticle.getId();
			}

			@Override
			public int getStatus() {
				return journalArticle.getStatus();
			}

			@Override
			public String getTitle(Locale locale) {
				return journalArticle.getTitle(locale);
			}

			@Override
			public String getTypeName(Locale locale) throws PortalException {
				DDMStructure ddmStructure = journalArticle.getDDMStructure();

				return ddmStructure.getName(locale);
			}

			@Override
			public String getUserName() {
				return journalArticle.getUserName();
			}

		};
	}

	private static final String _EXTERNAL_REFERENCE_CODE_INFIX_LAUNCH_DRAFT =
		"-launch-";

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private JournalArticleResourceLocalService
		_journalArticleResourceLocalService;

	@Reference
	private Language _language;

}