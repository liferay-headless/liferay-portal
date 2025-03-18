/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.graphql.servlet.v1_0;

import com.liferay.headless.admin.site.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.headless.admin.site.internal.graphql.query.v1_0.Query;
import com.liferay.headless.admin.site.internal.resource.v1_0.DisplayPageTemplateFolderResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.DisplayPageTemplateResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.FragmentCompositionResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.FriendlyUrlHistoryResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.MasterPageResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.PageElementResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.PageExperienceResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.PageRuleActionResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.PageRuleConditionResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.PageRuleResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.PageSpecificationResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.PageTemplateResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.PageTemplateSetResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.SitePageResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.UtilityPageResourceImpl;
import com.liferay.headless.admin.site.internal.resource.v1_0.WidgetPageWidgetInstanceResourceImpl;
import com.liferay.headless.admin.site.resource.v1_0.DisplayPageTemplateFolderResource;
import com.liferay.headless.admin.site.resource.v1_0.DisplayPageTemplateResource;
import com.liferay.headless.admin.site.resource.v1_0.FragmentCompositionResource;
import com.liferay.headless.admin.site.resource.v1_0.FriendlyUrlHistoryResource;
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
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * @author Rubén Pulido
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setDisplayPageTemplateResourceComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects);
		Mutation.setDisplayPageTemplateFolderResourceComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects);
		Mutation.setFragmentCompositionResourceComponentServiceObjects(
			_fragmentCompositionResourceComponentServiceObjects);
		Mutation.setMasterPageResourceComponentServiceObjects(
			_masterPageResourceComponentServiceObjects);
		Mutation.setPageElementResourceComponentServiceObjects(
			_pageElementResourceComponentServiceObjects);
		Mutation.setPageExperienceResourceComponentServiceObjects(
			_pageExperienceResourceComponentServiceObjects);
		Mutation.setPageRuleResourceComponentServiceObjects(
			_pageRuleResourceComponentServiceObjects);
		Mutation.setPageRuleActionResourceComponentServiceObjects(
			_pageRuleActionResourceComponentServiceObjects);
		Mutation.setPageRuleConditionResourceComponentServiceObjects(
			_pageRuleConditionResourceComponentServiceObjects);
		Mutation.setPageSpecificationResourceComponentServiceObjects(
			_pageSpecificationResourceComponentServiceObjects);
		Mutation.setPageTemplateResourceComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects);
		Mutation.setPageTemplateSetResourceComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects);
		Mutation.setSitePageResourceComponentServiceObjects(
			_sitePageResourceComponentServiceObjects);
		Mutation.setUtilityPageResourceComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects);
		Mutation.setWidgetPageWidgetInstanceResourceComponentServiceObjects(
			_widgetPageWidgetInstanceResourceComponentServiceObjects);

		Query.setDisplayPageTemplateResourceComponentServiceObjects(
			_displayPageTemplateResourceComponentServiceObjects);
		Query.setDisplayPageTemplateFolderResourceComponentServiceObjects(
			_displayPageTemplateFolderResourceComponentServiceObjects);
		Query.setFragmentCompositionResourceComponentServiceObjects(
			_fragmentCompositionResourceComponentServiceObjects);
		Query.setFriendlyUrlHistoryResourceComponentServiceObjects(
			_friendlyUrlHistoryResourceComponentServiceObjects);
		Query.setMasterPageResourceComponentServiceObjects(
			_masterPageResourceComponentServiceObjects);
		Query.setPageElementResourceComponentServiceObjects(
			_pageElementResourceComponentServiceObjects);
		Query.setPageExperienceResourceComponentServiceObjects(
			_pageExperienceResourceComponentServiceObjects);
		Query.setPageRuleResourceComponentServiceObjects(
			_pageRuleResourceComponentServiceObjects);
		Query.setPageRuleActionResourceComponentServiceObjects(
			_pageRuleActionResourceComponentServiceObjects);
		Query.setPageRuleConditionResourceComponentServiceObjects(
			_pageRuleConditionResourceComponentServiceObjects);
		Query.setPageSpecificationResourceComponentServiceObjects(
			_pageSpecificationResourceComponentServiceObjects);
		Query.setPageTemplateResourceComponentServiceObjects(
			_pageTemplateResourceComponentServiceObjects);
		Query.setPageTemplateSetResourceComponentServiceObjects(
			_pageTemplateSetResourceComponentServiceObjects);
		Query.setSitePageResourceComponentServiceObjects(
			_sitePageResourceComponentServiceObjects);
		Query.setUtilityPageResourceComponentServiceObjects(
			_utilityPageResourceComponentServiceObjects);
		Query.setWidgetPageWidgetInstanceResourceComponentServiceObjects(
			_widgetPageWidgetInstanceResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Headless.Admin.Site";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/headless-admin-site-graphql/v1_0";
	}

	@Override
	public Query getQuery() {
		return new Query();
	}

	public ObjectValuePair<Class<?>, String> getResourceMethodObjectValuePair(
		String methodName, boolean mutation) {

		if (mutation) {
			return _resourceMethodObjectValuePairs.get(
				"mutation#" + methodName);
		}

		return _resourceMethodObjectValuePairs.get("query#" + methodName);
	}

	private static final Map<String, ObjectValuePair<Class<?>, String>>
		_resourceMethodObjectValuePairs =
			new HashMap<String, ObjectValuePair<Class<?>, String>>() {
				{
					put(
						"mutation#createSiteDisplayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplate",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteDisplayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplate"));
					put(
						"mutation#createSiteDisplayPageTemplatesPageExportBatch",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteDisplayPageTemplatesPageExportBatch"));
					put(
						"mutation#createSiteDisplayPageTemplate",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteDisplayPageTemplate"));
					put(
						"mutation#createSiteDisplayPageTemplateBatch",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteDisplayPageTemplateBatch"));
					put(
						"mutation#updateSiteDisplayPageTemplatePermissionsPage",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"putSiteDisplayPageTemplatePermissionsPage"));
					put(
						"mutation#deleteSiteDisplayPageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"deleteSiteDisplayPageTemplateByExternalReferenceCode"));
					put(
						"mutation#patchSiteDisplayPageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"patchSiteDisplayPageTemplateByExternalReferenceCode"));
					put(
						"mutation#updateSiteDisplayPageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"putSiteDisplayPageTemplateByExternalReferenceCode"));
					put(
						"mutation#createSiteDisplayPageTemplateByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteDisplayPageTemplateByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSiteDisplayPageTemplateByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"putSiteDisplayPageTemplateByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteDisplayPageTemplateFoldersPageExportBatch",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"postSiteDisplayPageTemplateFoldersPageExportBatch"));
					put(
						"mutation#createSiteDisplayPageTemplateFolder",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"postSiteDisplayPageTemplateFolder"));
					put(
						"mutation#createSiteDisplayPageTemplateFolderBatch",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"postSiteDisplayPageTemplateFolderBatch"));
					put(
						"mutation#updateSiteDisplayPageTemplateFolderPermissionsPage",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"putSiteDisplayPageTemplateFolderPermissionsPage"));
					put(
						"mutation#deleteSiteDisplayPageTemplateFolderByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"deleteSiteDisplayPageTemplateFolderByExternalReferenceCode"));
					put(
						"mutation#patchSiteDisplayPageTemplateFolderByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"patchSiteDisplayPageTemplateFolderByExternalReferenceCode"));
					put(
						"mutation#updateSiteDisplayPageTemplateFolderByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"putSiteDisplayPageTemplateFolderByExternalReferenceCode"));
					put(
						"mutation#updateSiteDisplayPageTemplateFolderByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"putSiteDisplayPageTemplateFolderByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteFragmentCompositionsPageExportBatch",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"postSiteFragmentCompositionsPageExportBatch"));
					put(
						"mutation#createSiteFragmentComposition",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"postSiteFragmentComposition"));
					put(
						"mutation#createSiteFragmentCompositionBatch",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"postSiteFragmentCompositionBatch"));
					put(
						"mutation#deleteSiteFragmentCompositionByExternalReferenceCode",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"deleteSiteFragmentCompositionByExternalReferenceCode"));
					put(
						"mutation#patchSiteFragmentCompositionByExternalReferenceCode",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"patchSiteFragmentCompositionByExternalReferenceCode"));
					put(
						"mutation#updateSiteFragmentCompositionByExternalReferenceCode",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"putSiteFragmentCompositionByExternalReferenceCode"));
					put(
						"mutation#createSiteMasterPagesPageExportBatch",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"postSiteMasterPagesPageExportBatch"));
					put(
						"mutation#createSiteMasterPage",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"postSiteMasterPage"));
					put(
						"mutation#createSiteMasterPageBatch",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"postSiteMasterPageBatch"));
					put(
						"mutation#updateSiteMasterPagePermissionsPage",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"putSiteMasterPagePermissionsPage"));
					put(
						"mutation#deleteSiteMasterPageByExternalReferenceCode",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"deleteSiteMasterPageByExternalReferenceCode"));
					put(
						"mutation#patchSiteMasterPageByExternalReferenceCode",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"patchSiteMasterPageByExternalReferenceCode"));
					put(
						"mutation#updateSiteMasterPageByExternalReferenceCode",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"putSiteMasterPageByExternalReferenceCode"));
					put(
						"mutation#createSiteMasterPageByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"postSiteMasterPageByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSiteMasterPageByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"putSiteMasterPageByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElement",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"postSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElement"));
					put(
						"mutation#deleteSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"deleteSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode"));
					put(
						"mutation#patchSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"patchSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode"));
					put(
						"mutation#updateSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"putSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode"));
					put(
						"mutation#createSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodeFragmentComposition",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"postSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodeFragmentComposition"));
					put(
						"mutation#deleteSitePageExperienceByExternalReferenceCode",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"deleteSitePageExperienceByExternalReferenceCode"));
					put(
						"mutation#patchSitePageExperienceByExternalReferenceCode",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"patchSitePageExperienceByExternalReferenceCode"));
					put(
						"mutation#updateSitePageExperienceByExternalReferenceCode",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"putSitePageExperienceByExternalReferenceCode"));
					put(
						"mutation#createSitePageSpecificationByExternalReferenceCodePageExperience",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"postSitePageSpecificationByExternalReferenceCodePageExperience"));
					put(
						"mutation#createSitePageExperienceByExternalReferenceCodePageRule",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"postSitePageExperienceByExternalReferenceCodePageRule"));
					put(
						"mutation#deleteSitePageRuleByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"deleteSitePageRuleByExternalReferenceCode"));
					put(
						"mutation#patchSitePageRuleByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"patchSitePageRuleByExternalReferenceCode"));
					put(
						"mutation#updateSitePageRuleByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"putSitePageRuleByExternalReferenceCode"));
					put(
						"mutation#deleteSitePageRuleActionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"deleteSitePageRuleActionByExternalReferenceCode"));
					put(
						"mutation#patchSitePageRuleActionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"patchSitePageRuleActionByExternalReferenceCode"));
					put(
						"mutation#updateSitePageRuleActionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"putSitePageRuleActionByExternalReferenceCode"));
					put(
						"mutation#createSitePageRuleByExternalReferenceCodePageRuleAction",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"postSitePageRuleByExternalReferenceCodePageRuleAction"));
					put(
						"mutation#deleteSitePageRuleConditionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"deleteSitePageRuleConditionByExternalReferenceCode"));
					put(
						"mutation#patchSitePageRuleConditionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"patchSitePageRuleConditionByExternalReferenceCode"));
					put(
						"mutation#updateSitePageRuleConditionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"putSitePageRuleConditionByExternalReferenceCode"));
					put(
						"mutation#createSitePageRuleByExternalReferenceCodePageRuleCondition",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"postSitePageRuleByExternalReferenceCodePageRuleCondition"));
					put(
						"mutation#deleteSitePageSpecificationByExternalReferenceCode",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"deleteSitePageSpecificationByExternalReferenceCode"));
					put(
						"mutation#patchSitePageSpecificationByExternalReferenceCode",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"patchSitePageSpecificationByExternalReferenceCode"));
					put(
						"mutation#updateSitePageSpecificationByExternalReferenceCode",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"putSitePageSpecificationByExternalReferenceCode"));
					put(
						"mutation#createSitePageSpecificationByExternalReferenceCodePublish",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"postSitePageSpecificationByExternalReferenceCodePublish"));
					put(
						"mutation#createSitePageTemplateSetByExternalReferenceCodePageTemplate",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSitePageTemplateSetByExternalReferenceCodePageTemplate"));
					put(
						"mutation#createSitePageTemplatesPageExportBatch",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSitePageTemplatesPageExportBatch"));
					put(
						"mutation#createSitePageTemplate",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSitePageTemplate"));
					put(
						"mutation#createSitePageTemplateBatch",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSitePageTemplateBatch"));
					put(
						"mutation#updateSitePageTemplatePermissionsPage",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"putSitePageTemplatePermissionsPage"));
					put(
						"mutation#deleteSitePageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"deleteSitePageTemplateByExternalReferenceCode"));
					put(
						"mutation#patchSitePageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"patchSitePageTemplateByExternalReferenceCode"));
					put(
						"mutation#updateSitePageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"putSitePageTemplateByExternalReferenceCode"));
					put(
						"mutation#createSitePageTemplateByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSitePageTemplateByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSitePageTemplateByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"putSitePageTemplateByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSitePageTemplateSetsPageExportBatch",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"postSitePageTemplateSetsPageExportBatch"));
					put(
						"mutation#createSitePageTemplateSet",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"postSitePageTemplateSet"));
					put(
						"mutation#createSitePageTemplateSetBatch",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"postSitePageTemplateSetBatch"));
					put(
						"mutation#updateSitePageTemplateSetPermissionsPage",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"putSitePageTemplateSetPermissionsPage"));
					put(
						"mutation#deleteSitePageTemplateSetByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"deleteSitePageTemplateSetByExternalReferenceCode"));
					put(
						"mutation#patchSitePageTemplateSetByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"patchSitePageTemplateSetByExternalReferenceCode"));
					put(
						"mutation#updateSitePageTemplateSetByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"putSitePageTemplateSetByExternalReferenceCode"));
					put(
						"mutation#updateSitePageTemplateSetByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"putSitePageTemplateSetByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteSitePagesPageExportBatch",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"postSiteSitePagesPageExportBatch"));
					put(
						"mutation#createSiteSitePage",
						new ObjectValuePair<>(
							SitePageResourceImpl.class, "postSiteSitePage"));
					put(
						"mutation#createSiteSitePageBatch",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"postSiteSitePageBatch"));
					put(
						"mutation#updateSiteSitePagePermissionsPage",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"putSiteSitePagePermissionsPage"));
					put(
						"mutation#deleteSiteSitePageByExternalReferenceCode",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"deleteSiteSitePageByExternalReferenceCode"));
					put(
						"mutation#patchSiteSitePageByExternalReferenceCode",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"patchSiteSitePageByExternalReferenceCode"));
					put(
						"mutation#updateSiteSitePageByExternalReferenceCode",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"putSiteSitePageByExternalReferenceCode"));
					put(
						"mutation#createSiteSitePageByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"postSiteSitePageByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSiteSitePageByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"putSiteSitePageByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteUtilityPagesPageExportBatch",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"postSiteUtilityPagesPageExportBatch"));
					put(
						"mutation#createSiteUtilityPage",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"postSiteUtilityPage"));
					put(
						"mutation#createSiteUtilityPageBatch",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"postSiteUtilityPageBatch"));
					put(
						"mutation#updateSiteUtilityPagePermissionsPage",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"putSiteUtilityPagePermissionsPage"));
					put(
						"mutation#deleteSiteUtilityPageByExternalReferenceCode",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"deleteSiteUtilityPageByExternalReferenceCode"));
					put(
						"mutation#patchSiteUtilityPageByExternalReferenceCode",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"patchSiteUtilityPageByExternalReferenceCode"));
					put(
						"mutation#updateSiteUtilityPageByExternalReferenceCode",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"putSiteUtilityPageByExternalReferenceCode"));
					put(
						"mutation#createSiteUtilityPageByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"postSiteUtilityPageByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSiteUtilityPageByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"putSiteUtilityPageByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteSitePageByExternalReferenceCodeWidgetInstance",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"postSiteSitePageByExternalReferenceCodeWidgetInstance"));
					put(
						"mutation#deleteSiteSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"deleteSiteSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode"));
					put(
						"mutation#patchSiteSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"patchSiteSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode"));
					put(
						"mutation#updateSiteSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"putSiteSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode"));

					put(
						"query#displayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplates",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteDisplayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplatesPage"));
					put(
						"query#displayPageTemplates",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteDisplayPageTemplatesPage"));
					put(
						"query#displayPageTemplatePermissions",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteDisplayPageTemplatePermissionsPage"));
					put(
						"query#displayPageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteDisplayPageTemplateByExternalReferenceCode"));
					put(
						"query#displayPageTemplateByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteDisplayPageTemplateByExternalReferenceCodePermissionsPage"));
					put(
						"query#displayPageTemplateFolders",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"getSiteDisplayPageTemplateFoldersPage"));
					put(
						"query#displayPageTemplateFolderPermissions",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"getSiteDisplayPageTemplateFolderPermissionsPage"));
					put(
						"query#displayPageTemplateFolderByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"getSiteDisplayPageTemplateFolderByExternalReferenceCode"));
					put(
						"query#displayPageTemplateFolderByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"getSiteDisplayPageTemplateFolderByExternalReferenceCodePermissionsPage"));
					put(
						"query#fragmentCompositions",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"getSiteFragmentCompositionsPage"));
					put(
						"query#fragmentCompositionByExternalReferenceCode",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"getSiteFragmentCompositionByExternalReferenceCode"));
					put(
						"query#displayPageTemplateByExternalReferenceCodeFriendlyUrlHistory",
						new ObjectValuePair<>(
							FriendlyUrlHistoryResourceImpl.class,
							"getSiteDisplayPageTemplateByExternalReferenceCodeFriendlyUrlHistory"));
					put(
						"query#sitePageByExternalReferenceCodeFriendlyUrlHistory",
						new ObjectValuePair<>(
							FriendlyUrlHistoryResourceImpl.class,
							"getSiteSitePageByExternalReferenceCodeFriendlyUrlHistory"));
					put(
						"query#utilityPageByExternalReferenceCodeFriendlyUrlHistory",
						new ObjectValuePair<>(
							FriendlyUrlHistoryResourceImpl.class,
							"getSiteUtilityPageByExternalReferenceCodeFriendlyUrlHistory"));
					put(
						"query#masterPages",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"getSiteMasterPagesPage"));
					put(
						"query#masterPagePermissions",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"getSiteMasterPagePermissionsPage"));
					put(
						"query#masterPageByExternalReferenceCode",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"getSiteMasterPageByExternalReferenceCode"));
					put(
						"query#masterPageByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"getSiteMasterPageByExternalReferenceCodePermissionsPage"));
					put(
						"query#pageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElements",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"getSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementsPage"));
					put(
						"query#pageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"getSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode"));
					put(
						"query#pageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodePageElements",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"getSitePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodePageElementsPage"));
					put(
						"query#pageExperienceByExternalReferenceCode",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"getSitePageExperienceByExternalReferenceCode"));
					put(
						"query#pageSpecificationByExternalReferenceCodePageExperiences",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"getSitePageSpecificationByExternalReferenceCodePageExperiencesPage"));
					put(
						"query#pageExperienceByExternalReferenceCodePageRules",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"getSitePageExperienceByExternalReferenceCodePageRulesPage"));
					put(
						"query#pageRuleByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"getSitePageRuleByExternalReferenceCode"));
					put(
						"query#pageRuleActionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"getSitePageRuleActionByExternalReferenceCode"));
					put(
						"query#pageRuleByExternalReferenceCodePageRuleActions",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"getSitePageRuleByExternalReferenceCodePageRuleActionsPage"));
					put(
						"query#pageRuleConditionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"getSitePageRuleConditionByExternalReferenceCode"));
					put(
						"query#pageRuleByExternalReferenceCodePageRuleConditions",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"getSitePageRuleByExternalReferenceCodePageRuleConditionsPage"));
					put(
						"query#displayPageTemplateByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#masterPageByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteMasterPageByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#pageSpecificationByExternalReferenceCode",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSitePageSpecificationByExternalReferenceCode"));
					put(
						"query#pageTemplateByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSitePageTemplateByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#sitePageByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteSitePageByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#utilityPageByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteUtilityPageByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#pageTemplateSetByExternalReferenceCodePageTemplates",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSitePageTemplateSetByExternalReferenceCodePageTemplatesPage"));
					put(
						"query#pageTemplates",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSitePageTemplatesPage"));
					put(
						"query#pageTemplatePermissions",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSitePageTemplatePermissionsPage"));
					put(
						"query#pageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSitePageTemplateByExternalReferenceCode"));
					put(
						"query#pageTemplateByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSitePageTemplateByExternalReferenceCodePermissionsPage"));
					put(
						"query#pageTemplateSets",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"getSitePageTemplateSetsPage"));
					put(
						"query#pageTemplateSetPermissions",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"getSitePageTemplateSetPermissionsPage"));
					put(
						"query#pageTemplateSetByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"getSitePageTemplateSetByExternalReferenceCode"));
					put(
						"query#pageTemplateSetByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"getSitePageTemplateSetByExternalReferenceCodePermissionsPage"));
					put(
						"query#sitePages",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"getSiteSitePagesPage"));
					put(
						"query#sitePagePermissions",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"getSiteSitePagePermissionsPage"));
					put(
						"query#sitePageByExternalReferenceCode",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"getSiteSitePageByExternalReferenceCode"));
					put(
						"query#sitePageByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"getSiteSitePageByExternalReferenceCodePermissionsPage"));
					put(
						"query#utilityPages",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"getSiteUtilityPagesPage"));
					put(
						"query#utilityPagePermissions",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"getSiteUtilityPagePermissionsPage"));
					put(
						"query#utilityPageByExternalReferenceCode",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"getSiteUtilityPageByExternalReferenceCode"));
					put(
						"query#utilityPageByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"getSiteUtilityPageByExternalReferenceCodePermissionsPage"));
					put(
						"query#sitePageByExternalReferenceCodeWidgetInstances",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"getSiteSitePageByExternalReferenceCodeWidgetInstancesPage"));
					put(
						"query#sitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"getSiteSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<DisplayPageTemplateResource>
		_displayPageTemplateResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<DisplayPageTemplateFolderResource>
		_displayPageTemplateFolderResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<FragmentCompositionResource>
		_fragmentCompositionResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<MasterPageResource>
		_masterPageResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PageElementResource>
		_pageElementResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PageExperienceResource>
		_pageExperienceResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PageRuleResource>
		_pageRuleResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PageRuleActionResource>
		_pageRuleActionResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PageRuleConditionResource>
		_pageRuleConditionResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PageSpecificationResource>
		_pageSpecificationResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PageTemplateResource>
		_pageTemplateResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PageTemplateSetResource>
		_pageTemplateSetResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<SitePageResource>
		_sitePageResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<UtilityPageResource>
		_utilityPageResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<WidgetPageWidgetInstanceResource>
		_widgetPageWidgetInstanceResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<FriendlyUrlHistoryResource>
		_friendlyUrlHistoryResourceComponentServiceObjects;

}