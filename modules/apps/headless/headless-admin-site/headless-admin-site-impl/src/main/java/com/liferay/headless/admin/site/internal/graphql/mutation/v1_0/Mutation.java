/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.graphql.mutation.v1_0;

import com.liferay.headless.admin.site.dto.v1_0.ContentPageSpecification;
import com.liferay.headless.admin.site.dto.v1_0.DisplayPageTemplate;
import com.liferay.headless.admin.site.dto.v1_0.DisplayPageTemplateFolder;
import com.liferay.headless.admin.site.dto.v1_0.FragmentComposition;
import com.liferay.headless.admin.site.dto.v1_0.MasterPage;
import com.liferay.headless.admin.site.dto.v1_0.PageElement;
import com.liferay.headless.admin.site.dto.v1_0.PageExperience;
import com.liferay.headless.admin.site.dto.v1_0.PageRule;
import com.liferay.headless.admin.site.dto.v1_0.PageRuleAction;
import com.liferay.headless.admin.site.dto.v1_0.PageRuleCondition;
import com.liferay.headless.admin.site.dto.v1_0.PageSpecification;
import com.liferay.headless.admin.site.dto.v1_0.PageTemplate;
import com.liferay.headless.admin.site.dto.v1_0.PageTemplateSet;
import com.liferay.headless.admin.site.dto.v1_0.SitePage;
import com.liferay.headless.admin.site.dto.v1_0.UtilityPage;
import com.liferay.headless.admin.site.dto.v1_0.WidgetPageWidgetInstance;
import com.liferay.headless.admin.site.resource.v1_0.DisplayPageTemplateFolderResource;
import com.liferay.headless.admin.site.resource.v1_0.DisplayPageTemplateResource;
import com.liferay.headless.admin.site.resource.v1_0.FragmentCompositionResource;
import com.liferay.headless.admin.site.resource.v1_0.MasterPageResource;
import com.liferay.headless.admin.site.resource.v1_0.PageElementResource;
import com.liferay.headless.admin.site.resource.v1_0.PageExperienceResource;
import com.liferay.headless.admin.site.resource.v1_0.PageRuleActionResource;
import com.liferay.headless.admin.site.resource.v1_0.PageRuleConditionResource;
import com.liferay.headless.admin.site.resource.v1_0.PageRuleResource;
import com.liferay.headless.admin.site.resource.v1_0.PageSpecificationResource;
import com.liferay.headless.admin.site.resource.v1_0.PageTemplateResource;
import com.liferay.headless.admin.site.resource.v1_0.PageTemplateSetResource;
import com.liferay.headless.admin.site.resource.v1_0.SitePageResource;
import com.liferay.headless.admin.site.resource.v1_0.UtilityPageResource;
import com.liferay.headless.admin.site.resource.v1_0.WidgetPageWidgetInstanceResource;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.batch.engine.resource.VulcanBatchEngineExportTaskResource;
import com.liferay.portal.vulcan.batch.engine.resource.VulcanBatchEngineImportTaskResource;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.pagination.Page;

import java.util.function.BiFunction;

import javax.annotation.Generated;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.ComponentServiceObjects;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class Mutation {

	public static void setDisplayPageTemplateResourceComponentServiceObjects(
		ComponentServiceObjects<DisplayPageTemplateResource>
			displayPageTemplateResourceComponentServiceObjects) {

		_displayPageTemplateResourceComponentServiceObjects =
			displayPageTemplateResourceComponentServiceObjects;
	}

	public static void
		setDisplayPageTemplateFolderResourceComponentServiceObjects(
			ComponentServiceObjects<DisplayPageTemplateFolderResource>
				displayPageTemplateFolderResourceComponentServiceObjects) {

		_displayPageTemplateFolderResourceComponentServiceObjects =
			displayPageTemplateFolderResourceComponentServiceObjects;
	}

	public static void setFragmentCompositionResourceComponentServiceObjects(
		ComponentServiceObjects<FragmentCompositionResource>
			fragmentCompositionResourceComponentServiceObjects) {

		_fragmentCompositionResourceComponentServiceObjects =
			fragmentCompositionResourceComponentServiceObjects;
	}

	public static void setMasterPageResourceComponentServiceObjects(
		ComponentServiceObjects<MasterPageResource>
			masterPageResourceComponentServiceObjects) {

		_masterPageResourceComponentServiceObjects =
			masterPageResourceComponentServiceObjects;
	}

	public static void setPageElementResourceComponentServiceObjects(
		ComponentServiceObjects<PageElementResource>
			pageElementResourceComponentServiceObjects) {

		_pageElementResourceComponentServiceObjects =
			pageElementResourceComponentServiceObjects;
	}

	public static void setPageExperienceResourceComponentServiceObjects(
		ComponentServiceObjects<PageExperienceResource>
			pageExperienceResourceComponentServiceObjects) {

		_pageExperienceResourceComponentServiceObjects =
			pageExperienceResourceComponentServiceObjects;
	}

	public static void setPageRuleResourceComponentServiceObjects(
		ComponentServiceObjects<PageRuleResource>
			pageRuleResourceComponentServiceObjects) {

		_pageRuleResourceComponentServiceObjects =
			pageRuleResourceComponentServiceObjects;
	}

	public static void setPageRuleActionResourceComponentServiceObjects(
		ComponentServiceObjects<PageRuleActionResource>
			pageRuleActionResourceComponentServiceObjects) {

		_pageRuleActionResourceComponentServiceObjects =
			pageRuleActionResourceComponentServiceObjects;
	}

	public static void setPageRuleConditionResourceComponentServiceObjects(
		ComponentServiceObjects<PageRuleConditionResource>
			pageRuleConditionResourceComponentServiceObjects) {

		_pageRuleConditionResourceComponentServiceObjects =
			pageRuleConditionResourceComponentServiceObjects;
	}

	public static void setPageSpecificationResourceComponentServiceObjects(
		ComponentServiceObjects<PageSpecificationResource>
			pageSpecificationResourceComponentServiceObjects) {

		_pageSpecificationResourceComponentServiceObjects =
			pageSpecificationResourceComponentServiceObjects;
	}

	public static void setPageTemplateResourceComponentServiceObjects(
		ComponentServiceObjects<PageTemplateResource>
			pageTemplateResourceComponentServiceObjects) {

		_pageTemplateResourceComponentServiceObjects =
			pageTemplateResourceComponentServiceObjects;
	}

	public static void setPageTemplateSetResourceComponentServiceObjects(
		ComponentServiceObjects<PageTemplateSetResource>
			pageTemplateSetResourceComponentServiceObjects) {

		_pageTemplateSetResourceComponentServiceObjects =
			pageTemplateSetResourceComponentServiceObjects;
	}

	public static void setSitePageResourceComponentServiceObjects(
		ComponentServiceObjects<SitePageResource>
			sitePageResourceComponentServiceObjects) {

		_sitePageResourceComponentServiceObjects =
			sitePageResourceComponentServiceObjects;
	}

	public static void setUtilityPageResourceComponentServiceObjects(
		ComponentServiceObjects<UtilityPageResource>
			utilityPageResourceComponentServiceObjects) {

		_utilityPageResourceComponentServiceObjects =
			utilityPageResourceComponentServiceObjects;
	}

	public static void
		setWidgetPageWidgetInstanceResourceComponentServiceObjects(
			ComponentServiceObjects<WidgetPageWidgetInstanceResource>
				widgetPageWidgetInstanceResourceComponentServiceObjects) {

		_widgetPageWidgetInstanceResourceComponentServiceObjects =
			widgetPageWidgetInstanceResourceComponentServiceObjects;
	}

	@GraphQLField(
		description = "Adds a new display page template in draft status to a display page template folder."
	)
	public DisplayPageTemplate
			createSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplate(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateFolderExternalReferenceCode")
					String displayPageTemplateFolderExternalReferenceCode,
				@GraphQLName("displayPageTemplate") DisplayPageTemplate
					displayPageTemplate)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource ->
				displayPageTemplateResource.
					postSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplate(
						siteExternalReferenceCode,
						displayPageTemplateFolderExternalReferenceCode,
						displayPageTemplate));
	}

	@GraphQLField
	public Response
			createSiteByExternalReferenceCodeDisplayPageTemplatesPageExportBatch(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("search") String search,
				@GraphQLName("filter") String filterString,
				@GraphQLName("sort") String sortsString,
				@GraphQLName("callbackURL") String callbackURL,
				@GraphQLName("contentType") String contentType,
				@GraphQLName("fieldNames") String fieldNames)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource ->
				displayPageTemplateResource.
					postSiteByExternalReferenceCodeDisplayPageTemplatesPageExportBatch(
						siteExternalReferenceCode, search,
						_filterBiFunction.apply(
							displayPageTemplateResource, filterString),
						_sortsBiFunction.apply(
							displayPageTemplateResource, sortsString),
						callbackURL, contentType, fieldNames));
	}

	@GraphQLField(description = "Adds a new display page template")
	public DisplayPageTemplate
			createSiteByExternalReferenceCodeDisplayPageTemplate(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplate") DisplayPageTemplate
					displayPageTemplate)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource ->
				displayPageTemplateResource.
					postSiteByExternalReferenceCodeDisplayPageTemplate(
						siteExternalReferenceCode, displayPageTemplate));
	}

	@GraphQLField
	public Response createSiteByExternalReferenceCodeDisplayPageTemplateBatch(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("object") Object object)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource ->
				displayPageTemplateResource.
					postSiteByExternalReferenceCodeDisplayPageTemplateBatch(
						siteExternalReferenceCode, callbackURL, object));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeDisplayPageTemplatePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource -> {
				Page paginationPage =
					displayPageTemplateResource.
						putSiteByExternalReferenceCodeDisplayPageTemplatePermissionsPage(
							siteExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField(
		description = "Deletes a specific display page template of a site."
	)
	public boolean
			deleteSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateExternalReferenceCode") String
					displayPageTemplateExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource ->
				displayPageTemplateResource.
					deleteSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode(
						siteExternalReferenceCode,
						displayPageTemplateExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public DisplayPageTemplate
			patchSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateExternalReferenceCode") String
					displayPageTemplateExternalReferenceCode,
				@GraphQLName("displayPageTemplate") DisplayPageTemplate
					displayPageTemplate)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource ->
				displayPageTemplateResource.
					patchSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode(
						siteExternalReferenceCode,
						displayPageTemplateExternalReferenceCode,
						displayPageTemplate));
	}

	@GraphQLField(
		description = "Updates the display page template with the given external reference code, or creates it if it does not exist."
	)
	public DisplayPageTemplate
			updateSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateExternalReferenceCode") String
					displayPageTemplateExternalReferenceCode,
				@GraphQLName("displayPageTemplate") DisplayPageTemplate
					displayPageTemplate)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource ->
				displayPageTemplateResource.
					putSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode(
						siteExternalReferenceCode,
						displayPageTemplateExternalReferenceCode,
						displayPageTemplate));
	}

	@GraphQLField(
		description = "Adds a new page specification in draft status to a display page template."
	)
	public ContentPageSpecification
			createSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecification(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateExternalReferenceCode") String
					displayPageTemplateExternalReferenceCode,
				@GraphQLName("contentPageSpecification")
					ContentPageSpecification contentPageSpecification)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource ->
				displayPageTemplateResource.
					postSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecification(
						siteExternalReferenceCode,
						displayPageTemplateExternalReferenceCode,
						contentPageSpecification));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateExternalReferenceCode") String
					displayPageTemplateExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateResource -> {
				Page paginationPage =
					displayPageTemplateResource.
						putSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePermissionsPage(
							siteExternalReferenceCode,
							displayPageTemplateExternalReferenceCode,
							permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField
	public Response
			createSiteByExternalReferenceCodeDisplayPageTemplateFoldersPageExportBatch(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("search") String search,
				@GraphQLName("filter") String filterString,
				@GraphQLName("sort") String sortsString,
				@GraphQLName("callbackURL") String callbackURL,
				@GraphQLName("contentType") String contentType,
				@GraphQLName("fieldNames") String fieldNames)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateFolderResource ->
				displayPageTemplateFolderResource.
					postSiteByExternalReferenceCodeDisplayPageTemplateFoldersPageExportBatch(
						siteExternalReferenceCode, search,
						_filterBiFunction.apply(
							displayPageTemplateFolderResource, filterString),
						_sortsBiFunction.apply(
							displayPageTemplateFolderResource, sortsString),
						callbackURL, contentType, fieldNames));
	}

	@GraphQLField(description = "Adds a new display page template folder.")
	public DisplayPageTemplateFolder
			createSiteByExternalReferenceCodeDisplayPageTemplateFolder(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateFolder")
					DisplayPageTemplateFolder displayPageTemplateFolder)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateFolderResource ->
				displayPageTemplateFolderResource.
					postSiteByExternalReferenceCodeDisplayPageTemplateFolder(
						siteExternalReferenceCode, displayPageTemplateFolder));
	}

	@GraphQLField
	public Response
			createSiteByExternalReferenceCodeDisplayPageTemplateFolderBatch(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("callbackURL") String callbackURL,
				@GraphQLName("object") Object object)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateFolderResource ->
				displayPageTemplateFolderResource.
					postSiteByExternalReferenceCodeDisplayPageTemplateFolderBatch(
						siteExternalReferenceCode, callbackURL, object));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeDisplayPageTemplateFolderPermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateFolderResource -> {
				Page paginationPage =
					displayPageTemplateFolderResource.
						putSiteByExternalReferenceCodeDisplayPageTemplateFolderPermissionsPage(
							siteExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField(
		description = "Deletes a specific display page template folder of a site."
	)
	public boolean
			deleteSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateFolderExternalReferenceCode")
					String displayPageTemplateFolderExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateFolderResource ->
				displayPageTemplateFolderResource.
					deleteSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode(
						siteExternalReferenceCode,
						displayPageTemplateFolderExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public DisplayPageTemplateFolder
			patchSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateFolderExternalReferenceCode")
					String displayPageTemplateFolderExternalReferenceCode,
				@GraphQLName("displayPageTemplateFolder")
					DisplayPageTemplateFolder displayPageTemplateFolder)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateFolderResource ->
				displayPageTemplateFolderResource.
					patchSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode(
						siteExternalReferenceCode,
						displayPageTemplateFolderExternalReferenceCode,
						displayPageTemplateFolder));
	}

	@GraphQLField(
		description = "Updates the display page template folder with the given external reference code, or creates it if it does not exist."
	)
	public DisplayPageTemplateFolder
			updateSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateFolderExternalReferenceCode")
					String displayPageTemplateFolderExternalReferenceCode,
				@GraphQLName("displayPageTemplateFolder")
					DisplayPageTemplateFolder displayPageTemplateFolder)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateFolderResource ->
				displayPageTemplateFolderResource.
					putSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode(
						siteExternalReferenceCode,
						displayPageTemplateFolderExternalReferenceCode,
						displayPageTemplateFolder));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("displayPageTemplateFolderExternalReferenceCode")
					String displayPageTemplateFolderExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects,
			this::_populateResourceContext,
			displayPageTemplateFolderResource -> {
				Page paginationPage =
					displayPageTemplateFolderResource.
						putSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodePermissionsPage(
							siteExternalReferenceCode,
							displayPageTemplateFolderExternalReferenceCode,
							permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField
	public Response
			createSiteByExternalReferenceCodeFragmentCompositionsPageExportBatch(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("search") String search,
				@GraphQLName("filter") String filterString,
				@GraphQLName("sort") String sortsString,
				@GraphQLName("callbackURL") String callbackURL,
				@GraphQLName("contentType") String contentType,
				@GraphQLName("fieldNames") String fieldNames)
		throws Exception {

		return _applyComponentServiceObjects(
			_fragmentCompositionResourceComponentServiceObjects,
			this::_populateResourceContext,
			fragmentCompositionResource ->
				fragmentCompositionResource.
					postSiteByExternalReferenceCodeFragmentCompositionsPageExportBatch(
						siteExternalReferenceCode, search,
						_filterBiFunction.apply(
							fragmentCompositionResource, filterString),
						_sortsBiFunction.apply(
							fragmentCompositionResource, sortsString),
						callbackURL, contentType, fieldNames));
	}

	@GraphQLField(
		description = "Adds a new fragment composition. If the page element of the fragment composition does not contain a definition property and contains an external reference code, the page element will be retrieved based on the externalReferenceCode and used for creating the fragment composition."
	)
	public FragmentComposition
			createSiteByExternalReferenceCodeFragmentComposition(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("saveInlineContent") Boolean saveInlineContent,
				@GraphQLName("saveMapping") Boolean saveMapping,
				@GraphQLName("fragmentComposition") FragmentComposition
					fragmentComposition)
		throws Exception {

		return _applyComponentServiceObjects(
			_fragmentCompositionResourceComponentServiceObjects,
			this::_populateResourceContext,
			fragmentCompositionResource ->
				fragmentCompositionResource.
					postSiteByExternalReferenceCodeFragmentComposition(
						siteExternalReferenceCode, saveInlineContent,
						saveMapping, fragmentComposition));
	}

	@GraphQLField
	public Response createSiteByExternalReferenceCodeFragmentCompositionBatch(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("saveInlineContent") Boolean saveInlineContent,
			@GraphQLName("saveMapping") Boolean saveMapping,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("object") Object object)
		throws Exception {

		return _applyComponentServiceObjects(
			_fragmentCompositionResourceComponentServiceObjects,
			this::_populateResourceContext,
			fragmentCompositionResource ->
				fragmentCompositionResource.
					postSiteByExternalReferenceCodeFragmentCompositionBatch(
						siteExternalReferenceCode, saveInlineContent,
						saveMapping, callbackURL, object));
	}

	@GraphQLField(
		description = "Deletes a specific fragment composition of a site."
	)
	public boolean
			deleteSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("fragmentCompositionExternalReferenceCode") String
					fragmentCompositionExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_fragmentCompositionResourceComponentServiceObjects,
			this::_populateResourceContext,
			fragmentCompositionResource ->
				fragmentCompositionResource.
					deleteSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode(
						siteExternalReferenceCode,
						fragmentCompositionExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public FragmentComposition
			patchSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("fragmentCompositionExternalReferenceCode") String
					fragmentCompositionExternalReferenceCode,
				@GraphQLName("fragmentComposition") FragmentComposition
					fragmentComposition)
		throws Exception {

		return _applyComponentServiceObjects(
			_fragmentCompositionResourceComponentServiceObjects,
			this::_populateResourceContext,
			fragmentCompositionResource ->
				fragmentCompositionResource.
					patchSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode(
						siteExternalReferenceCode,
						fragmentCompositionExternalReferenceCode,
						fragmentComposition));
	}

	@GraphQLField(
		description = "Updates the fragment composition with the given external reference code, or creates it if it does not exist."
	)
	public FragmentComposition
			updateSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("fragmentCompositionExternalReferenceCode") String
					fragmentCompositionExternalReferenceCode,
				@GraphQLName("fragmentComposition") FragmentComposition
					fragmentComposition)
		throws Exception {

		return _applyComponentServiceObjects(
			_fragmentCompositionResourceComponentServiceObjects,
			this::_populateResourceContext,
			fragmentCompositionResource ->
				fragmentCompositionResource.
					putSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode(
						siteExternalReferenceCode,
						fragmentCompositionExternalReferenceCode,
						fragmentComposition));
	}

	@GraphQLField
	public Response createSiteByExternalReferenceCodeMasterPagesPageExportBatch(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("search") String search,
			@GraphQLName("filter") String filterString,
			@GraphQLName("sort") String sortsString,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("contentType") String contentType,
			@GraphQLName("fieldNames") String fieldNames)
		throws Exception {

		return _applyComponentServiceObjects(
			_masterPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			masterPageResource ->
				masterPageResource.
					postSiteByExternalReferenceCodeMasterPagesPageExportBatch(
						siteExternalReferenceCode, search,
						_filterBiFunction.apply(
							masterPageResource, filterString),
						_sortsBiFunction.apply(masterPageResource, sortsString),
						callbackURL, contentType, fieldNames));
	}

	@GraphQLField(description = "Adds a new master page.")
	public MasterPage createSiteByExternalReferenceCodeMasterPage(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("masterPage") MasterPage masterPage)
		throws Exception {

		return _applyComponentServiceObjects(
			_masterPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			masterPageResource ->
				masterPageResource.postSiteByExternalReferenceCodeMasterPage(
					siteExternalReferenceCode, masterPage));
	}

	@GraphQLField
	public Response createSiteByExternalReferenceCodeMasterPageBatch(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("object") Object object)
		throws Exception {

		return _applyComponentServiceObjects(
			_masterPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			masterPageResource ->
				masterPageResource.
					postSiteByExternalReferenceCodeMasterPageBatch(
						siteExternalReferenceCode, callbackURL, object));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeMasterPagePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_masterPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			masterPageResource -> {
				Page paginationPage =
					masterPageResource.
						putSiteByExternalReferenceCodeMasterPagePermissionsPage(
							siteExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField(description = "Deletes a specific master page of a site.")
	public boolean
			deleteSiteByExternalReferenceCodeMasterPageByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("masterPageExternalReferenceCode") String
					masterPageExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_masterPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			masterPageResource ->
				masterPageResource.
					deleteSiteByExternalReferenceCodeMasterPageByExternalReferenceCode(
						siteExternalReferenceCode,
						masterPageExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public MasterPage
			patchSiteByExternalReferenceCodeMasterPageByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("masterPageExternalReferenceCode") String
					masterPageExternalReferenceCode,
				@GraphQLName("masterPage") MasterPage masterPage)
		throws Exception {

		return _applyComponentServiceObjects(
			_masterPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			masterPageResource ->
				masterPageResource.
					patchSiteByExternalReferenceCodeMasterPageByExternalReferenceCode(
						siteExternalReferenceCode,
						masterPageExternalReferenceCode, masterPage));
	}

	@GraphQLField(
		description = "Updates the master page with the given external reference code, or creates it if it does not exist."
	)
	public MasterPage
			updateSiteByExternalReferenceCodeMasterPageByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("masterPageExternalReferenceCode") String
					masterPageExternalReferenceCode,
				@GraphQLName("masterPage") MasterPage masterPage)
		throws Exception {

		return _applyComponentServiceObjects(
			_masterPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			masterPageResource ->
				masterPageResource.
					putSiteByExternalReferenceCodeMasterPageByExternalReferenceCode(
						siteExternalReferenceCode,
						masterPageExternalReferenceCode, masterPage));
	}

	@GraphQLField(
		description = "Adds a new page specification in draft status to a master page."
	)
	public ContentPageSpecification
			createSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecification(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("masterPageExternalReferenceCode") String
					masterPageExternalReferenceCode,
				@GraphQLName("contentPageSpecification")
					ContentPageSpecification contentPageSpecification)
		throws Exception {

		return _applyComponentServiceObjects(
			_masterPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			masterPageResource ->
				masterPageResource.
					postSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecification(
						siteExternalReferenceCode,
						masterPageExternalReferenceCode,
						contentPageSpecification));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("masterPageExternalReferenceCode") String
					masterPageExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_masterPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			masterPageResource -> {
				Page paginationPage =
					masterPageResource.
						putSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePermissionsPage(
							siteExternalReferenceCode,
							masterPageExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField(
		description = "Adds a new page element to an experience in a page specification in draft status of a site page."
	)
	public PageElement
			createSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElement(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode,
				@GraphQLName("pageExperienceExternalReferenceCode") String
					pageExperienceExternalReferenceCode,
				@GraphQLName("pageElement") PageElement pageElement)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageElementResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageElementResource ->
				pageElementResource.
					postSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElement(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode,
						pageExperienceExternalReferenceCode, pageElement));
	}

	@GraphQLField(
		description = "Deletes a page element within an experience of a specific page specification of a site page within a site."
	)
	public boolean
			deleteSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode,
				@GraphQLName("pageExperienceExternalReferenceCode") String
					pageExperienceExternalReferenceCode,
				@GraphQLName("pageElementExternalReferenceCode") String
					pageElementExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_pageElementResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageElementResource ->
				pageElementResource.
					deleteSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode,
						pageExperienceExternalReferenceCode,
						pageElementExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates a page element within an experience of a specific page specification of a site page within a site. Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public PageElement
			patchSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode,
				@GraphQLName("pageExperienceExternalReferenceCode") String
					pageExperienceExternalReferenceCode,
				@GraphQLName("pageElementExternalReferenceCode") String
					pageElementExternalReferenceCode,
				@GraphQLName("pageElement") PageElement pageElement)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageElementResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageElementResource ->
				pageElementResource.
					patchSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode,
						pageExperienceExternalReferenceCode,
						pageElementExternalReferenceCode, pageElement));
	}

	@GraphQLField(
		description = "Updates a page element within an experience of a specific page specification of a site page within a site."
	)
	public PageElement
			updateSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode,
				@GraphQLName("pageExperienceExternalReferenceCode") String
					pageExperienceExternalReferenceCode,
				@GraphQLName("pageElementExternalReferenceCode") String
					pageElementExternalReferenceCode,
				@GraphQLName("pageElement") PageElement pageElement)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageElementResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageElementResource ->
				pageElementResource.
					putSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode,
						pageExperienceExternalReferenceCode,
						pageElementExternalReferenceCode, pageElement));
	}

	@GraphQLField(
		description = "Adds a new fragment composition under a page element of an experience in a page specification of a site page. If successful, the response will contain the page element in which the fragment composition is converted."
	)
	public PageElement
			createSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodeFragmentComposition(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode,
				@GraphQLName("pageExperienceExternalReferenceCode") String
					pageExperienceExternalReferenceCode,
				@GraphQLName("pageElementExternalReferenceCode") String
					pageElementExternalReferenceCode,
				@GraphQLName("position") Integer position,
				@GraphQLName("fragmentComposition") FragmentComposition
					fragmentComposition)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageElementResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageElementResource ->
				pageElementResource.
					postSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodeFragmentComposition(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode,
						pageExperienceExternalReferenceCode,
						pageElementExternalReferenceCode, position,
						fragmentComposition));
	}

	@GraphQLField(
		description = "Deletes an experience of a specific page specification of a site page within a site. The default experience cannot be deleted."
	)
	public boolean
			deleteSiteByExternalReferenceCodePageExperienceByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageExperienceExternalReferenceCode") String
					pageExperienceExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_pageExperienceResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageExperienceResource ->
				pageExperienceResource.
					deleteSiteByExternalReferenceCodePageExperienceByExternalReferenceCode(
						siteExternalReferenceCode,
						pageExperienceExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates an experience of a specific page specification of a site page within a site. Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public PageExperience
			patchSiteByExternalReferenceCodePageExperienceByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageExperienceExternalReferenceCode") String
					pageExperienceExternalReferenceCode,
				@GraphQLName("pageExperience") PageExperience pageExperience)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageExperienceResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageExperienceResource ->
				pageExperienceResource.
					patchSiteByExternalReferenceCodePageExperienceByExternalReferenceCode(
						siteExternalReferenceCode,
						pageExperienceExternalReferenceCode, pageExperience));
	}

	@GraphQLField(
		description = "Updates an experience of a specific page specification of a site page within a site."
	)
	public PageExperience
			updateSiteByExternalReferenceCodePageExperienceByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageExperienceExternalReferenceCode") String
					pageExperienceExternalReferenceCode,
				@GraphQLName("pageExperience") PageExperience pageExperience)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageExperienceResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageExperienceResource ->
				pageExperienceResource.
					putSiteByExternalReferenceCodePageExperienceByExternalReferenceCode(
						siteExternalReferenceCode,
						pageExperienceExternalReferenceCode, pageExperience));
	}

	@GraphQLField(
		description = "Adds a new experience to a page specification of a site page."
	)
	public PageExperience
			createSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperience(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode,
				@GraphQLName("pageExperience") PageExperience pageExperience)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageExperienceResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageExperienceResource ->
				pageExperienceResource.
					postSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperience(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode,
						pageExperience));
	}

	@GraphQLField(
		description = "Adds a new page rule to an experience in a page specification in draft status of a site page."
	)
	public PageRule
			createSiteByExternalReferenceCodePageExperienceByExternalReferenceCodePageRule(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageExperienceExternalReferenceCode") String
					pageExperienceExternalReferenceCode,
				@GraphQLName("pageRule") PageRule pageRule)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageRuleResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleResource ->
				pageRuleResource.
					postSiteByExternalReferenceCodePageExperienceByExternalReferenceCodePageRule(
						siteExternalReferenceCode,
						pageExperienceExternalReferenceCode, pageRule));
	}

	@GraphQLField(
		description = "Deletes a page rule within an experience of a specific page specification of a site page within a site."
	)
	public boolean
			deleteSiteByExternalReferenceCodePageRuleByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleExternalReferenceCode") String
					pageRuleExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_pageRuleResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleResource ->
				pageRuleResource.
					deleteSiteByExternalReferenceCodePageRuleByExternalReferenceCode(
						siteExternalReferenceCode,
						pageRuleExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates a page rule within an experience of a specific page specification of a site page within a site. Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public PageRule
			patchSiteByExternalReferenceCodePageRuleByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleExternalReferenceCode") String
					pageRuleExternalReferenceCode,
				@GraphQLName("pageRule") PageRule pageRule)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageRuleResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleResource ->
				pageRuleResource.
					patchSiteByExternalReferenceCodePageRuleByExternalReferenceCode(
						siteExternalReferenceCode,
						pageRuleExternalReferenceCode, pageRule));
	}

	@GraphQLField(
		description = "Updates a page rule within an experience of a specific page specification of a site page within a site."
	)
	public PageRule
			updateSiteByExternalReferenceCodePageRuleByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleExternalReferenceCode") String
					pageRuleExternalReferenceCode,
				@GraphQLName("pageRule") PageRule pageRule)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageRuleResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleResource ->
				pageRuleResource.
					putSiteByExternalReferenceCodePageRuleByExternalReferenceCode(
						siteExternalReferenceCode,
						pageRuleExternalReferenceCode, pageRule));
	}

	@GraphQLField(
		description = "Deletes a page rule action within a page rule of an experience of a specific page specification of a site page within a site."
	)
	public boolean
			deleteSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleActionExternalReferenceCode") String
					pageRuleActionExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_pageRuleActionResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleActionResource ->
				pageRuleActionResource.
					deleteSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode(
						siteExternalReferenceCode,
						pageRuleActionExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates a page rule action within a page rule of an experience of a specific page specification of a site page within a site. Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public PageRuleAction
			patchSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleActionExternalReferenceCode") String
					pageRuleActionExternalReferenceCode,
				@GraphQLName("pageRuleAction") PageRuleAction pageRuleAction)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageRuleActionResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleActionResource ->
				pageRuleActionResource.
					patchSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode(
						siteExternalReferenceCode,
						pageRuleActionExternalReferenceCode, pageRuleAction));
	}

	@GraphQLField(
		description = "Updates a page rule action within a page rule of an experience of a specific page specification of a site page within a site."
	)
	public PageRuleAction
			updateSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleActionExternalReferenceCode") String
					pageRuleActionExternalReferenceCode,
				@GraphQLName("pageRuleAction") PageRuleAction pageRuleAction)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageRuleActionResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleActionResource ->
				pageRuleActionResource.
					putSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode(
						siteExternalReferenceCode,
						pageRuleActionExternalReferenceCode, pageRuleAction));
	}

	@GraphQLField(
		description = "Adds a new page rule action to a page rule in an experience in a page specification in draft status of a site page."
	)
	public PageRuleAction
			createSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleAction(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleExternalReferenceCode") String
					pageRuleExternalReferenceCode,
				@GraphQLName("pageRuleAction") PageRuleAction pageRuleAction)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageRuleActionResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleActionResource ->
				pageRuleActionResource.
					postSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleAction(
						siteExternalReferenceCode,
						pageRuleExternalReferenceCode, pageRuleAction));
	}

	@GraphQLField(
		description = "Deletes a page rule condition within a page rule of an experience of a specific page specification of a site page within a site."
	)
	public boolean
			deleteSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleConditionExternalReferenceCode") String
					pageRuleConditionExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_pageRuleConditionResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleConditionResource ->
				pageRuleConditionResource.
					deleteSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode(
						siteExternalReferenceCode,
						pageRuleConditionExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates a page rule condition within a page rule of an experience of a specific page specification of a site page within a site. Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public PageRuleCondition
			patchSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleConditionExternalReferenceCode") String
					pageRuleConditionExternalReferenceCode,
				@GraphQLName("pageRuleCondition") PageRuleCondition
					pageRuleCondition)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageRuleConditionResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleConditionResource ->
				pageRuleConditionResource.
					patchSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode(
						siteExternalReferenceCode,
						pageRuleConditionExternalReferenceCode,
						pageRuleCondition));
	}

	@GraphQLField(
		description = "Updates a page rule condition within a page rule of an experience of a specific page specification of a site page within a site."
	)
	public PageRuleCondition
			updateSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleConditionExternalReferenceCode") String
					pageRuleConditionExternalReferenceCode,
				@GraphQLName("pageRuleCondition") PageRuleCondition
					pageRuleCondition)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageRuleConditionResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleConditionResource ->
				pageRuleConditionResource.
					putSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode(
						siteExternalReferenceCode,
						pageRuleConditionExternalReferenceCode,
						pageRuleCondition));
	}

	@GraphQLField(
		description = "Adds a new page rule condition to a page rule in an experience in a page specification in draft status of a site page."
	)
	public PageRuleCondition
			createSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleCondition(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageRuleExternalReferenceCode") String
					pageRuleExternalReferenceCode,
				@GraphQLName("pageRuleCondition") PageRuleCondition
					pageRuleCondition)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageRuleConditionResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageRuleConditionResource ->
				pageRuleConditionResource.
					postSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleCondition(
						siteExternalReferenceCode,
						pageRuleExternalReferenceCode, pageRuleCondition));
	}

	@GraphQLField(description = "Deletes a page specification of a site page.")
	public boolean
			deleteSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_pageSpecificationResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageSpecificationResource ->
				pageSpecificationResource.
					deleteSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates a page specification of a site page. Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public PageSpecification
			patchSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode,
				@GraphQLName("pageSpecification") PageSpecification
					pageSpecification)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageSpecificationResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageSpecificationResource ->
				pageSpecificationResource.
					patchSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode,
						pageSpecification));
	}

	@GraphQLField(description = "Updates a page specification of a site page.")
	public PageSpecification
			updateSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode,
				@GraphQLName("pageSpecification") PageSpecification
					pageSpecification)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageSpecificationResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageSpecificationResource ->
				pageSpecificationResource.
					putSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode,
						pageSpecification));
	}

	@GraphQLField(
		description = "Publishes a page specification in draft status of a site page."
	)
	public PageSpecification
			createSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePublish(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageSpecificationExternalReferenceCode") String
					pageSpecificationExternalReferenceCode)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageSpecificationResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageSpecificationResource ->
				pageSpecificationResource.
					postSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePublish(
						siteExternalReferenceCode,
						pageSpecificationExternalReferenceCode));
	}

	@GraphQLField(
		description = "Adds a new page template in draft status to a page template set."
	)
	public PageTemplate
			createSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePageTemplate(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateSetExternalReferenceCode") String
					pageTemplateSetExternalReferenceCode,
				@GraphQLName("pageTemplate") PageTemplate pageTemplate)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource ->
				pageTemplateResource.
					postSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePageTemplate(
						siteExternalReferenceCode,
						pageTemplateSetExternalReferenceCode, pageTemplate));
	}

	@GraphQLField
	public Response
			createSiteByExternalReferenceCodePageTemplatesPageExportBatch(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("search") String search,
				@GraphQLName("filter") String filterString,
				@GraphQLName("sort") String sortsString,
				@GraphQLName("callbackURL") String callbackURL,
				@GraphQLName("contentType") String contentType,
				@GraphQLName("fieldNames") String fieldNames)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource ->
				pageTemplateResource.
					postSiteByExternalReferenceCodePageTemplatesPageExportBatch(
						siteExternalReferenceCode, search,
						_filterBiFunction.apply(
							pageTemplateResource, filterString),
						_sortsBiFunction.apply(
							pageTemplateResource, sortsString),
						callbackURL, contentType, fieldNames));
	}

	@GraphQLField(description = "Adds a new page template")
	public PageTemplate createSiteByExternalReferenceCodePageTemplate(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("pageTemplate") PageTemplate pageTemplate)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource ->
				pageTemplateResource.
					postSiteByExternalReferenceCodePageTemplate(
						siteExternalReferenceCode, pageTemplate));
	}

	@GraphQLField
	public Response createSiteByExternalReferenceCodePageTemplateBatch(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("object") Object object)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource ->
				pageTemplateResource.
					postSiteByExternalReferenceCodePageTemplateBatch(
						siteExternalReferenceCode, callbackURL, object));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodePageTemplatePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource -> {
				Page paginationPage =
					pageTemplateResource.
						putSiteByExternalReferenceCodePageTemplatePermissionsPage(
							siteExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField(description = "Deletes a specific page template of a site.")
	public boolean
			deleteSiteByExternalReferenceCodePageTemplateByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateExternalReferenceCode") String
					pageTemplateExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource ->
				pageTemplateResource.
					deleteSiteByExternalReferenceCodePageTemplateByExternalReferenceCode(
						siteExternalReferenceCode,
						pageTemplateExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public PageTemplate
			patchSiteByExternalReferenceCodePageTemplateByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateExternalReferenceCode") String
					pageTemplateExternalReferenceCode,
				@GraphQLName("pageTemplate") PageTemplate pageTemplate)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource ->
				pageTemplateResource.
					patchSiteByExternalReferenceCodePageTemplateByExternalReferenceCode(
						siteExternalReferenceCode,
						pageTemplateExternalReferenceCode, pageTemplate));
	}

	@GraphQLField(
		description = "Updates the page template with the given external reference code, or creates it if it does not exist."
	)
	public PageTemplate
			updateSiteByExternalReferenceCodePageTemplateByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateExternalReferenceCode") String
					pageTemplateExternalReferenceCode,
				@GraphQLName("pageTemplate") PageTemplate pageTemplate)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource ->
				pageTemplateResource.
					putSiteByExternalReferenceCodePageTemplateByExternalReferenceCode(
						siteExternalReferenceCode,
						pageTemplateExternalReferenceCode, pageTemplate));
	}

	@GraphQLField(
		description = "Adds a new page specification in draft status to a page template."
	)
	public ContentPageSpecification
			createSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecification(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateExternalReferenceCode") String
					pageTemplateExternalReferenceCode,
				@GraphQLName("contentPageSpecification")
					ContentPageSpecification contentPageSpecification)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource ->
				pageTemplateResource.
					postSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecification(
						siteExternalReferenceCode,
						pageTemplateExternalReferenceCode,
						contentPageSpecification));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateExternalReferenceCode") String
					pageTemplateExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateResource -> {
				Page paginationPage =
					pageTemplateResource.
						putSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePermissionsPage(
							siteExternalReferenceCode,
							pageTemplateExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField
	public Response
			createSiteByExternalReferenceCodePageTemplateSetsPageExportBatch(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("search") String search,
				@GraphQLName("filter") String filterString,
				@GraphQLName("sort") String sortsString,
				@GraphQLName("callbackURL") String callbackURL,
				@GraphQLName("contentType") String contentType,
				@GraphQLName("fieldNames") String fieldNames)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateSetResource ->
				pageTemplateSetResource.
					postSiteByExternalReferenceCodePageTemplateSetsPageExportBatch(
						siteExternalReferenceCode, search,
						_filterBiFunction.apply(
							pageTemplateSetResource, filterString),
						_sortsBiFunction.apply(
							pageTemplateSetResource, sortsString),
						callbackURL, contentType, fieldNames));
	}

	@GraphQLField(description = "Adds a new page template set")
	public PageTemplateSet createSiteByExternalReferenceCodePageTemplateSet(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("pageTemplateSet") PageTemplateSet pageTemplateSet)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateSetResource ->
				pageTemplateSetResource.
					postSiteByExternalReferenceCodePageTemplateSet(
						siteExternalReferenceCode, pageTemplateSet));
	}

	@GraphQLField
	public Response createSiteByExternalReferenceCodePageTemplateSetBatch(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("object") Object object)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateSetResource ->
				pageTemplateSetResource.
					postSiteByExternalReferenceCodePageTemplateSetBatch(
						siteExternalReferenceCode, callbackURL, object));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodePageTemplateSetPermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateSetResource -> {
				Page paginationPage =
					pageTemplateSetResource.
						putSiteByExternalReferenceCodePageTemplateSetPermissionsPage(
							siteExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField(
		description = "Deletes a specific page template set of a site."
	)
	public boolean
			deleteSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateSetExternalReferenceCode") String
					pageTemplateSetExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateSetResource ->
				pageTemplateSetResource.
					deleteSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode(
						siteExternalReferenceCode,
						pageTemplateSetExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public PageTemplateSet
			patchSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateSetExternalReferenceCode") String
					pageTemplateSetExternalReferenceCode,
				@GraphQLName("pageTemplateSet") PageTemplateSet pageTemplateSet)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateSetResource ->
				pageTemplateSetResource.
					patchSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode(
						siteExternalReferenceCode,
						pageTemplateSetExternalReferenceCode, pageTemplateSet));
	}

	@GraphQLField(
		description = "Updates the page template set with the given external reference code, or creates it if it does not exist."
	)
	public PageTemplateSet
			updateSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateSetExternalReferenceCode") String
					pageTemplateSetExternalReferenceCode,
				@GraphQLName("pageTemplateSet") PageTemplateSet pageTemplateSet)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateSetResource ->
				pageTemplateSetResource.
					putSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode(
						siteExternalReferenceCode,
						pageTemplateSetExternalReferenceCode, pageTemplateSet));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("pageTemplateSetExternalReferenceCode") String
					pageTemplateSetExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects,
			this::_populateResourceContext,
			pageTemplateSetResource -> {
				Page paginationPage =
					pageTemplateSetResource.
						putSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePermissionsPage(
							siteExternalReferenceCode,
							pageTemplateSetExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField
	public Response createSiteByExternalReferenceCodeSitePagesPageExportBatch(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("search") String search,
			@GraphQLName("filter") String filterString,
			@GraphQLName("sort") String sortsString,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("contentType") String contentType,
			@GraphQLName("fieldNames") String fieldNames)
		throws Exception {

		return _applyComponentServiceObjects(
			_sitePageResourceComponentServiceObjects,
			this::_populateResourceContext,
			sitePageResource ->
				sitePageResource.
					postSiteByExternalReferenceCodeSitePagesPageExportBatch(
						siteExternalReferenceCode, search,
						_filterBiFunction.apply(sitePageResource, filterString),
						_sortsBiFunction.apply(sitePageResource, sortsString),
						callbackURL, contentType, fieldNames));
	}

	@GraphQLField(description = "Adds a new site page")
	public SitePage createSiteByExternalReferenceCodeSitePage(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("sitePage") SitePage sitePage)
		throws Exception {

		return _applyComponentServiceObjects(
			_sitePageResourceComponentServiceObjects,
			this::_populateResourceContext,
			sitePageResource ->
				sitePageResource.postSiteByExternalReferenceCodeSitePage(
					siteExternalReferenceCode, sitePage));
	}

	@GraphQLField
	public Response createSiteByExternalReferenceCodeSitePageBatch(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("object") Object object)
		throws Exception {

		return _applyComponentServiceObjects(
			_sitePageResourceComponentServiceObjects,
			this::_populateResourceContext,
			sitePageResource ->
				sitePageResource.postSiteByExternalReferenceCodeSitePageBatch(
					siteExternalReferenceCode, callbackURL, object));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeSitePagePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_sitePageResourceComponentServiceObjects,
			this::_populateResourceContext,
			sitePageResource -> {
				Page paginationPage =
					sitePageResource.
						putSiteByExternalReferenceCodeSitePagePermissionsPage(
							siteExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField(description = "Deletes a specific public page of a site.")
	public boolean
			deleteSiteByExternalReferenceCodeSitePageByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("sitePageExternalReferenceCode") String
					sitePageExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_sitePageResourceComponentServiceObjects,
			this::_populateResourceContext,
			sitePageResource ->
				sitePageResource.
					deleteSiteByExternalReferenceCodeSitePageByExternalReferenceCode(
						siteExternalReferenceCode,
						sitePageExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public SitePage
			patchSiteByExternalReferenceCodeSitePageByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("sitePageExternalReferenceCode") String
					sitePageExternalReferenceCode,
				@GraphQLName("sitePage") SitePage sitePage)
		throws Exception {

		return _applyComponentServiceObjects(
			_sitePageResourceComponentServiceObjects,
			this::_populateResourceContext,
			sitePageResource ->
				sitePageResource.
					patchSiteByExternalReferenceCodeSitePageByExternalReferenceCode(
						siteExternalReferenceCode,
						sitePageExternalReferenceCode, sitePage));
	}

	@GraphQLField(
		description = "Updates the site page with the given external reference code, or creates it if it does not exist."
	)
	public SitePage
			updateSiteByExternalReferenceCodeSitePageByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("sitePageExternalReferenceCode") String
					sitePageExternalReferenceCode,
				@GraphQLName("sitePage") SitePage sitePage)
		throws Exception {

		return _applyComponentServiceObjects(
			_sitePageResourceComponentServiceObjects,
			this::_populateResourceContext,
			sitePageResource ->
				sitePageResource.
					putSiteByExternalReferenceCodeSitePageByExternalReferenceCode(
						siteExternalReferenceCode,
						sitePageExternalReferenceCode, sitePage));
	}

	@GraphQLField(description = "Adds a new page specification to a site page.")
	public ContentPageSpecification
			createSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecification(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("sitePageExternalReferenceCode") String
					sitePageExternalReferenceCode,
				@GraphQLName("contentPageSpecification")
					ContentPageSpecification contentPageSpecification)
		throws Exception {

		return _applyComponentServiceObjects(
			_sitePageResourceComponentServiceObjects,
			this::_populateResourceContext,
			sitePageResource ->
				sitePageResource.
					postSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecification(
						siteExternalReferenceCode,
						sitePageExternalReferenceCode,
						contentPageSpecification));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeSitePageByExternalReferenceCodePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("sitePageExternalReferenceCode") String
					sitePageExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_sitePageResourceComponentServiceObjects,
			this::_populateResourceContext,
			sitePageResource -> {
				Page paginationPage =
					sitePageResource.
						putSiteByExternalReferenceCodeSitePageByExternalReferenceCodePermissionsPage(
							siteExternalReferenceCode,
							sitePageExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField
	public Response
			createSiteByExternalReferenceCodeUtilityPagesPageExportBatch(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("search") String search,
				@GraphQLName("filter") String filterString,
				@GraphQLName("sort") String sortsString,
				@GraphQLName("callbackURL") String callbackURL,
				@GraphQLName("contentType") String contentType,
				@GraphQLName("fieldNames") String fieldNames)
		throws Exception {

		return _applyComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			utilityPageResource ->
				utilityPageResource.
					postSiteByExternalReferenceCodeUtilityPagesPageExportBatch(
						siteExternalReferenceCode, search,
						_filterBiFunction.apply(
							utilityPageResource, filterString),
						_sortsBiFunction.apply(
							utilityPageResource, sortsString),
						callbackURL, contentType, fieldNames));
	}

	@GraphQLField(description = "Adds a new utility page")
	public UtilityPage createSiteByExternalReferenceCodeUtilityPage(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("utilityPage") UtilityPage utilityPage)
		throws Exception {

		return _applyComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			utilityPageResource ->
				utilityPageResource.postSiteByExternalReferenceCodeUtilityPage(
					siteExternalReferenceCode, utilityPage));
	}

	@GraphQLField
	public Response createSiteByExternalReferenceCodeUtilityPageBatch(
			@GraphQLName("siteExternalReferenceCode") String
				siteExternalReferenceCode,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("object") Object object)
		throws Exception {

		return _applyComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			utilityPageResource ->
				utilityPageResource.
					postSiteByExternalReferenceCodeUtilityPageBatch(
						siteExternalReferenceCode, callbackURL, object));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeUtilityPagePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			utilityPageResource -> {
				Page paginationPage =
					utilityPageResource.
						putSiteByExternalReferenceCodeUtilityPagePermissionsPage(
							siteExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField(description = "Deletes a specific utility page of a site.")
	public boolean
			deleteSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("utilityPageExternalReferenceCode") String
					utilityPageExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			utilityPageResource ->
				utilityPageResource.
					deleteSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode(
						siteExternalReferenceCode,
						utilityPageExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public UtilityPage
			patchSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("utilityPageExternalReferenceCode") String
					utilityPageExternalReferenceCode,
				@GraphQLName("utilityPage") UtilityPage utilityPage)
		throws Exception {

		return _applyComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			utilityPageResource ->
				utilityPageResource.
					patchSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode(
						siteExternalReferenceCode,
						utilityPageExternalReferenceCode, utilityPage));
	}

	@GraphQLField(
		description = "Updates the utility page with the given external reference code, or creates it if it does not exist."
	)
	public UtilityPage
			updateSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("utilityPageExternalReferenceCode") String
					utilityPageExternalReferenceCode,
				@GraphQLName("utilityPage") UtilityPage utilityPage)
		throws Exception {

		return _applyComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			utilityPageResource ->
				utilityPageResource.
					putSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode(
						siteExternalReferenceCode,
						utilityPageExternalReferenceCode, utilityPage));
	}

	@GraphQLField(
		description = "Adds a new page specification to a utility page."
	)
	public ContentPageSpecification
			createSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecification(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("utilityPageExternalReferenceCode") String
					utilityPageExternalReferenceCode,
				@GraphQLName("contentPageSpecification")
					ContentPageSpecification contentPageSpecification)
		throws Exception {

		return _applyComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			utilityPageResource ->
				utilityPageResource.
					postSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecification(
						siteExternalReferenceCode,
						utilityPageExternalReferenceCode,
						contentPageSpecification));
	}

	@GraphQLField
	public java.util.Collection<com.liferay.portal.vulcan.permission.Permission>
			updateSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePermissionsPage(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("utilityPageExternalReferenceCode") String
					utilityPageExternalReferenceCode,
				@GraphQLName("permissions")
					com.liferay.portal.vulcan.permission.Permission[]
						permissions)
		throws Exception {

		return _applyComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects,
			this::_populateResourceContext,
			utilityPageResource -> {
				Page paginationPage =
					utilityPageResource.
						putSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePermissionsPage(
							siteExternalReferenceCode,
							utilityPageExternalReferenceCode, permissions);

				return paginationPage.getItems();
			});
	}

	@GraphQLField(description = "Adds a new widget instance to a widget page.")
	public WidgetPageWidgetInstance
			createSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstance(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("sitePageExternalReferenceCode") String
					sitePageExternalReferenceCode,
				@GraphQLName("widgetPageWidgetInstance")
					WidgetPageWidgetInstance widgetPageWidgetInstance)
		throws Exception {

		return _applyComponentServiceObjects(
			_widgetPageWidgetInstanceResourceComponentServiceObjects,
			this::_populateResourceContext,
			widgetPageWidgetInstanceResource ->
				widgetPageWidgetInstanceResource.
					postSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstance(
						siteExternalReferenceCode,
						sitePageExternalReferenceCode,
						widgetPageWidgetInstance));
	}

	@GraphQLField(
		description = "Deletes a widget instance of a specific widget page or widget page template within a site."
	)
	public boolean
			deleteSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("sitePageExternalReferenceCode") String
					sitePageExternalReferenceCode,
				@GraphQLName("widgetInstanceExternalReferenceCode") String
					widgetInstanceExternalReferenceCode)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_widgetPageWidgetInstanceResourceComponentServiceObjects,
			this::_populateResourceContext,
			widgetPageWidgetInstanceResource ->
				widgetPageWidgetInstanceResource.
					deleteSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode(
						siteExternalReferenceCode,
						sitePageExternalReferenceCode,
						widgetInstanceExternalReferenceCode));

		return true;
	}

	@GraphQLField(
		description = "Updates a widget instance of a widget page or widget page template within a site. Updates only the fields received in the request body, leaving any other fields untouched."
	)
	public WidgetPageWidgetInstance
			patchSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("sitePageExternalReferenceCode") String
					sitePageExternalReferenceCode,
				@GraphQLName("widgetInstanceExternalReferenceCode") String
					widgetInstanceExternalReferenceCode,
				@GraphQLName("widgetPageWidgetInstance")
					WidgetPageWidgetInstance widgetPageWidgetInstance)
		throws Exception {

		return _applyComponentServiceObjects(
			_widgetPageWidgetInstanceResourceComponentServiceObjects,
			this::_populateResourceContext,
			widgetPageWidgetInstanceResource ->
				widgetPageWidgetInstanceResource.
					patchSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode(
						siteExternalReferenceCode,
						sitePageExternalReferenceCode,
						widgetInstanceExternalReferenceCode,
						widgetPageWidgetInstance));
	}

	@GraphQLField(
		description = "Updates a widget instance of a widget page or widget page template within a site."
	)
	public WidgetPageWidgetInstance
			updateSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode(
				@GraphQLName("siteExternalReferenceCode") String
					siteExternalReferenceCode,
				@GraphQLName("sitePageExternalReferenceCode") String
					sitePageExternalReferenceCode,
				@GraphQLName("widgetInstanceExternalReferenceCode") String
					widgetInstanceExternalReferenceCode,
				@GraphQLName("widgetPageWidgetInstance")
					WidgetPageWidgetInstance widgetPageWidgetInstance)
		throws Exception {

		return _applyComponentServiceObjects(
			_widgetPageWidgetInstanceResourceComponentServiceObjects,
			this::_populateResourceContext,
			widgetPageWidgetInstanceResource ->
				widgetPageWidgetInstanceResource.
					putSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode(
						siteExternalReferenceCode,
						sitePageExternalReferenceCode,
						widgetInstanceExternalReferenceCode,
						widgetPageWidgetInstance));
	}

	private <T, R, E1 extends Throwable, E2 extends Throwable> R
			_applyComponentServiceObjects(
				ComponentServiceObjects<T> componentServiceObjects,
				UnsafeConsumer<T, E1> unsafeConsumer,
				UnsafeFunction<T, R, E2> unsafeFunction)
		throws E1, E2 {

		T resource = componentServiceObjects.getService();

		try {
			unsafeConsumer.accept(resource);

			return unsafeFunction.apply(resource);
		}
		finally {
			componentServiceObjects.ungetService(resource);
		}
	}

	private <T, E1 extends Throwable, E2 extends Throwable> void
			_applyVoidComponentServiceObjects(
				ComponentServiceObjects<T> componentServiceObjects,
				UnsafeConsumer<T, E1> unsafeConsumer,
				UnsafeConsumer<T, E2> unsafeFunction)
		throws E1, E2 {

		T resource = componentServiceObjects.getService();

		try {
			unsafeConsumer.accept(resource);

			unsafeFunction.accept(resource);
		}
		finally {
			componentServiceObjects.ungetService(resource);
		}
	}

	private void _populateResourceContext(
			DisplayPageTemplateResource displayPageTemplateResource)
		throws Exception {

		displayPageTemplateResource.setContextAcceptLanguage(_acceptLanguage);
		displayPageTemplateResource.setContextCompany(_company);
		displayPageTemplateResource.setContextHttpServletRequest(
			_httpServletRequest);
		displayPageTemplateResource.setContextHttpServletResponse(
			_httpServletResponse);
		displayPageTemplateResource.setContextUriInfo(_uriInfo);
		displayPageTemplateResource.setContextUser(_user);
		displayPageTemplateResource.setGroupLocalService(_groupLocalService);
		displayPageTemplateResource.setRoleLocalService(_roleLocalService);

		displayPageTemplateResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		displayPageTemplateResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			DisplayPageTemplateFolderResource displayPageTemplateFolderResource)
		throws Exception {

		displayPageTemplateFolderResource.setContextAcceptLanguage(
			_acceptLanguage);
		displayPageTemplateFolderResource.setContextCompany(_company);
		displayPageTemplateFolderResource.setContextHttpServletRequest(
			_httpServletRequest);
		displayPageTemplateFolderResource.setContextHttpServletResponse(
			_httpServletResponse);
		displayPageTemplateFolderResource.setContextUriInfo(_uriInfo);
		displayPageTemplateFolderResource.setContextUser(_user);
		displayPageTemplateFolderResource.setGroupLocalService(
			_groupLocalService);
		displayPageTemplateFolderResource.setRoleLocalService(
			_roleLocalService);

		displayPageTemplateFolderResource.
			setVulcanBatchEngineExportTaskResource(
				_vulcanBatchEngineExportTaskResource);

		displayPageTemplateFolderResource.
			setVulcanBatchEngineImportTaskResource(
				_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			FragmentCompositionResource fragmentCompositionResource)
		throws Exception {

		fragmentCompositionResource.setContextAcceptLanguage(_acceptLanguage);
		fragmentCompositionResource.setContextCompany(_company);
		fragmentCompositionResource.setContextHttpServletRequest(
			_httpServletRequest);
		fragmentCompositionResource.setContextHttpServletResponse(
			_httpServletResponse);
		fragmentCompositionResource.setContextUriInfo(_uriInfo);
		fragmentCompositionResource.setContextUser(_user);
		fragmentCompositionResource.setGroupLocalService(_groupLocalService);
		fragmentCompositionResource.setRoleLocalService(_roleLocalService);

		fragmentCompositionResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		fragmentCompositionResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(MasterPageResource masterPageResource)
		throws Exception {

		masterPageResource.setContextAcceptLanguage(_acceptLanguage);
		masterPageResource.setContextCompany(_company);
		masterPageResource.setContextHttpServletRequest(_httpServletRequest);
		masterPageResource.setContextHttpServletResponse(_httpServletResponse);
		masterPageResource.setContextUriInfo(_uriInfo);
		masterPageResource.setContextUser(_user);
		masterPageResource.setGroupLocalService(_groupLocalService);
		masterPageResource.setRoleLocalService(_roleLocalService);

		masterPageResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		masterPageResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			PageElementResource pageElementResource)
		throws Exception {

		pageElementResource.setContextAcceptLanguage(_acceptLanguage);
		pageElementResource.setContextCompany(_company);
		pageElementResource.setContextHttpServletRequest(_httpServletRequest);
		pageElementResource.setContextHttpServletResponse(_httpServletResponse);
		pageElementResource.setContextUriInfo(_uriInfo);
		pageElementResource.setContextUser(_user);
		pageElementResource.setGroupLocalService(_groupLocalService);
		pageElementResource.setRoleLocalService(_roleLocalService);

		pageElementResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		pageElementResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			PageExperienceResource pageExperienceResource)
		throws Exception {

		pageExperienceResource.setContextAcceptLanguage(_acceptLanguage);
		pageExperienceResource.setContextCompany(_company);
		pageExperienceResource.setContextHttpServletRequest(
			_httpServletRequest);
		pageExperienceResource.setContextHttpServletResponse(
			_httpServletResponse);
		pageExperienceResource.setContextUriInfo(_uriInfo);
		pageExperienceResource.setContextUser(_user);
		pageExperienceResource.setGroupLocalService(_groupLocalService);
		pageExperienceResource.setRoleLocalService(_roleLocalService);

		pageExperienceResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		pageExperienceResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(PageRuleResource pageRuleResource)
		throws Exception {

		pageRuleResource.setContextAcceptLanguage(_acceptLanguage);
		pageRuleResource.setContextCompany(_company);
		pageRuleResource.setContextHttpServletRequest(_httpServletRequest);
		pageRuleResource.setContextHttpServletResponse(_httpServletResponse);
		pageRuleResource.setContextUriInfo(_uriInfo);
		pageRuleResource.setContextUser(_user);
		pageRuleResource.setGroupLocalService(_groupLocalService);
		pageRuleResource.setRoleLocalService(_roleLocalService);

		pageRuleResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		pageRuleResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			PageRuleActionResource pageRuleActionResource)
		throws Exception {

		pageRuleActionResource.setContextAcceptLanguage(_acceptLanguage);
		pageRuleActionResource.setContextCompany(_company);
		pageRuleActionResource.setContextHttpServletRequest(
			_httpServletRequest);
		pageRuleActionResource.setContextHttpServletResponse(
			_httpServletResponse);
		pageRuleActionResource.setContextUriInfo(_uriInfo);
		pageRuleActionResource.setContextUser(_user);
		pageRuleActionResource.setGroupLocalService(_groupLocalService);
		pageRuleActionResource.setRoleLocalService(_roleLocalService);

		pageRuleActionResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		pageRuleActionResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			PageRuleConditionResource pageRuleConditionResource)
		throws Exception {

		pageRuleConditionResource.setContextAcceptLanguage(_acceptLanguage);
		pageRuleConditionResource.setContextCompany(_company);
		pageRuleConditionResource.setContextHttpServletRequest(
			_httpServletRequest);
		pageRuleConditionResource.setContextHttpServletResponse(
			_httpServletResponse);
		pageRuleConditionResource.setContextUriInfo(_uriInfo);
		pageRuleConditionResource.setContextUser(_user);
		pageRuleConditionResource.setGroupLocalService(_groupLocalService);
		pageRuleConditionResource.setRoleLocalService(_roleLocalService);

		pageRuleConditionResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		pageRuleConditionResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			PageSpecificationResource pageSpecificationResource)
		throws Exception {

		pageSpecificationResource.setContextAcceptLanguage(_acceptLanguage);
		pageSpecificationResource.setContextCompany(_company);
		pageSpecificationResource.setContextHttpServletRequest(
			_httpServletRequest);
		pageSpecificationResource.setContextHttpServletResponse(
			_httpServletResponse);
		pageSpecificationResource.setContextUriInfo(_uriInfo);
		pageSpecificationResource.setContextUser(_user);
		pageSpecificationResource.setGroupLocalService(_groupLocalService);
		pageSpecificationResource.setRoleLocalService(_roleLocalService);

		pageSpecificationResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		pageSpecificationResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			PageTemplateResource pageTemplateResource)
		throws Exception {

		pageTemplateResource.setContextAcceptLanguage(_acceptLanguage);
		pageTemplateResource.setContextCompany(_company);
		pageTemplateResource.setContextHttpServletRequest(_httpServletRequest);
		pageTemplateResource.setContextHttpServletResponse(
			_httpServletResponse);
		pageTemplateResource.setContextUriInfo(_uriInfo);
		pageTemplateResource.setContextUser(_user);
		pageTemplateResource.setGroupLocalService(_groupLocalService);
		pageTemplateResource.setRoleLocalService(_roleLocalService);

		pageTemplateResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		pageTemplateResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			PageTemplateSetResource pageTemplateSetResource)
		throws Exception {

		pageTemplateSetResource.setContextAcceptLanguage(_acceptLanguage);
		pageTemplateSetResource.setContextCompany(_company);
		pageTemplateSetResource.setContextHttpServletRequest(
			_httpServletRequest);
		pageTemplateSetResource.setContextHttpServletResponse(
			_httpServletResponse);
		pageTemplateSetResource.setContextUriInfo(_uriInfo);
		pageTemplateSetResource.setContextUser(_user);
		pageTemplateSetResource.setGroupLocalService(_groupLocalService);
		pageTemplateSetResource.setRoleLocalService(_roleLocalService);

		pageTemplateSetResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		pageTemplateSetResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(SitePageResource sitePageResource)
		throws Exception {

		sitePageResource.setContextAcceptLanguage(_acceptLanguage);
		sitePageResource.setContextCompany(_company);
		sitePageResource.setContextHttpServletRequest(_httpServletRequest);
		sitePageResource.setContextHttpServletResponse(_httpServletResponse);
		sitePageResource.setContextUriInfo(_uriInfo);
		sitePageResource.setContextUser(_user);
		sitePageResource.setGroupLocalService(_groupLocalService);
		sitePageResource.setRoleLocalService(_roleLocalService);

		sitePageResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		sitePageResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			UtilityPageResource utilityPageResource)
		throws Exception {

		utilityPageResource.setContextAcceptLanguage(_acceptLanguage);
		utilityPageResource.setContextCompany(_company);
		utilityPageResource.setContextHttpServletRequest(_httpServletRequest);
		utilityPageResource.setContextHttpServletResponse(_httpServletResponse);
		utilityPageResource.setContextUriInfo(_uriInfo);
		utilityPageResource.setContextUser(_user);
		utilityPageResource.setGroupLocalService(_groupLocalService);
		utilityPageResource.setRoleLocalService(_roleLocalService);

		utilityPageResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		utilityPageResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private void _populateResourceContext(
			WidgetPageWidgetInstanceResource widgetPageWidgetInstanceResource)
		throws Exception {

		widgetPageWidgetInstanceResource.setContextAcceptLanguage(
			_acceptLanguage);
		widgetPageWidgetInstanceResource.setContextCompany(_company);
		widgetPageWidgetInstanceResource.setContextHttpServletRequest(
			_httpServletRequest);
		widgetPageWidgetInstanceResource.setContextHttpServletResponse(
			_httpServletResponse);
		widgetPageWidgetInstanceResource.setContextUriInfo(_uriInfo);
		widgetPageWidgetInstanceResource.setContextUser(_user);
		widgetPageWidgetInstanceResource.setGroupLocalService(
			_groupLocalService);
		widgetPageWidgetInstanceResource.setRoleLocalService(_roleLocalService);

		widgetPageWidgetInstanceResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		widgetPageWidgetInstanceResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private static ComponentServiceObjects<DisplayPageTemplateResource>
		_displayPageTemplateResourceComponentServiceObjects;
	private static ComponentServiceObjects<DisplayPageTemplateFolderResource>
		_displayPageTemplateFolderResourceComponentServiceObjects;
	private static ComponentServiceObjects<FragmentCompositionResource>
		_fragmentCompositionResourceComponentServiceObjects;
	private static ComponentServiceObjects<MasterPageResource>
		_masterPageResourceComponentServiceObjects;
	private static ComponentServiceObjects<PageElementResource>
		_pageElementResourceComponentServiceObjects;
	private static ComponentServiceObjects<PageExperienceResource>
		_pageExperienceResourceComponentServiceObjects;
	private static ComponentServiceObjects<PageRuleResource>
		_pageRuleResourceComponentServiceObjects;
	private static ComponentServiceObjects<PageRuleActionResource>
		_pageRuleActionResourceComponentServiceObjects;
	private static ComponentServiceObjects<PageRuleConditionResource>
		_pageRuleConditionResourceComponentServiceObjects;
	private static ComponentServiceObjects<PageSpecificationResource>
		_pageSpecificationResourceComponentServiceObjects;
	private static ComponentServiceObjects<PageTemplateResource>
		_pageTemplateResourceComponentServiceObjects;
	private static ComponentServiceObjects<PageTemplateSetResource>
		_pageTemplateSetResourceComponentServiceObjects;
	private static ComponentServiceObjects<SitePageResource>
		_sitePageResourceComponentServiceObjects;
	private static ComponentServiceObjects<UtilityPageResource>
		_utilityPageResourceComponentServiceObjects;
	private static ComponentServiceObjects<WidgetPageWidgetInstanceResource>
		_widgetPageWidgetInstanceResourceComponentServiceObjects;

	private AcceptLanguage _acceptLanguage;
	private com.liferay.portal.kernel.model.Company _company;
	private BiFunction
		<Object, String, com.liferay.portal.kernel.search.filter.Filter>
			_filterBiFunction;
	private GroupLocalService _groupLocalService;
	private HttpServletRequest _httpServletRequest;
	private HttpServletResponse _httpServletResponse;
	private RoleLocalService _roleLocalService;
	private BiFunction<Object, String, com.liferay.portal.kernel.search.Sort[]>
		_sortsBiFunction;
	private UriInfo _uriInfo;
	private com.liferay.portal.kernel.model.User _user;
	private VulcanBatchEngineExportTaskResource
		_vulcanBatchEngineExportTaskResource;
	private VulcanBatchEngineImportTaskResource
		_vulcanBatchEngineImportTaskResource;

}