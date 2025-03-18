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
						"mutation#createSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplate",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplate"));
					put(
						"mutation#createSiteByExternalReferenceCodeDisplayPageTemplatesPageExportBatch",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodeDisplayPageTemplatesPageExportBatch"));
					put(
						"mutation#createSiteByExternalReferenceCodeDisplayPageTemplate",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodeDisplayPageTemplate"));
					put(
						"mutation#createSiteByExternalReferenceCodeDisplayPageTemplateBatch",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodeDisplayPageTemplateBatch"));
					put(
						"mutation#updateSiteByExternalReferenceCodeDisplayPageTemplatePermissionsPage",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"putSiteByExternalReferenceCodeDisplayPageTemplatePermissionsPage"));
					put(
						"mutation#deleteSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"deleteSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"patchSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"putSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"putSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteByExternalReferenceCodeDisplayPageTemplateFoldersPageExportBatch",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"postSiteByExternalReferenceCodeDisplayPageTemplateFoldersPageExportBatch"));
					put(
						"mutation#createSiteByExternalReferenceCodeDisplayPageTemplateFolder",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"postSiteByExternalReferenceCodeDisplayPageTemplateFolder"));
					put(
						"mutation#createSiteByExternalReferenceCodeDisplayPageTemplateFolderBatch",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"postSiteByExternalReferenceCodeDisplayPageTemplateFolderBatch"));
					put(
						"mutation#updateSiteByExternalReferenceCodeDisplayPageTemplateFolderPermissionsPage",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"putSiteByExternalReferenceCodeDisplayPageTemplateFolderPermissionsPage"));
					put(
						"mutation#deleteSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"deleteSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"patchSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"putSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"putSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteByExternalReferenceCodeFragmentCompositionsPageExportBatch",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"postSiteByExternalReferenceCodeFragmentCompositionsPageExportBatch"));
					put(
						"mutation#createSiteByExternalReferenceCodeFragmentComposition",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"postSiteByExternalReferenceCodeFragmentComposition"));
					put(
						"mutation#createSiteByExternalReferenceCodeFragmentCompositionBatch",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"postSiteByExternalReferenceCodeFragmentCompositionBatch"));
					put(
						"mutation#deleteSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"deleteSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"patchSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"putSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodeMasterPagesPageExportBatch",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"postSiteByExternalReferenceCodeMasterPagesPageExportBatch"));
					put(
						"mutation#createSiteByExternalReferenceCodeMasterPage",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"postSiteByExternalReferenceCodeMasterPage"));
					put(
						"mutation#createSiteByExternalReferenceCodeMasterPageBatch",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"postSiteByExternalReferenceCodeMasterPageBatch"));
					put(
						"mutation#updateSiteByExternalReferenceCodeMasterPagePermissionsPage",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"putSiteByExternalReferenceCodeMasterPagePermissionsPage"));
					put(
						"mutation#deleteSiteByExternalReferenceCodeMasterPageByExternalReferenceCode",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"deleteSiteByExternalReferenceCodeMasterPageByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodeMasterPageByExternalReferenceCode",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"patchSiteByExternalReferenceCodeMasterPageByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodeMasterPageByExternalReferenceCode",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"putSiteByExternalReferenceCodeMasterPageByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"postSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"putSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElement",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"postSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElement"));
					put(
						"mutation#deleteSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"deleteSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"patchSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"putSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodeFragmentComposition",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"postSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodeFragmentComposition"));
					put(
						"mutation#deleteSiteByExternalReferenceCodePageExperienceByExternalReferenceCode",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"deleteSiteByExternalReferenceCodePageExperienceByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodePageExperienceByExternalReferenceCode",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"patchSiteByExternalReferenceCodePageExperienceByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageExperienceByExternalReferenceCode",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"putSiteByExternalReferenceCodePageExperienceByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperience",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"postSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperience"));
					put(
						"mutation#createSiteByExternalReferenceCodePageExperienceByExternalReferenceCodePageRule",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"postSiteByExternalReferenceCodePageExperienceByExternalReferenceCodePageRule"));
					put(
						"mutation#deleteSiteByExternalReferenceCodePageRuleByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"deleteSiteByExternalReferenceCodePageRuleByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodePageRuleByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"patchSiteByExternalReferenceCodePageRuleByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageRuleByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"putSiteByExternalReferenceCodePageRuleByExternalReferenceCode"));
					put(
						"mutation#deleteSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"deleteSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"patchSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"putSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleAction",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"postSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleAction"));
					put(
						"mutation#deleteSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"deleteSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"patchSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"putSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleCondition",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"postSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleCondition"));
					put(
						"mutation#deleteSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"deleteSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"patchSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"putSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePublish",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"postSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePublish"));
					put(
						"mutation#createSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePageTemplate",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePageTemplate"));
					put(
						"mutation#createSiteByExternalReferenceCodePageTemplatesPageExportBatch",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodePageTemplatesPageExportBatch"));
					put(
						"mutation#createSiteByExternalReferenceCodePageTemplate",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodePageTemplate"));
					put(
						"mutation#createSiteByExternalReferenceCodePageTemplateBatch",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodePageTemplateBatch"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageTemplatePermissionsPage",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"putSiteByExternalReferenceCodePageTemplatePermissionsPage"));
					put(
						"mutation#deleteSiteByExternalReferenceCodePageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"deleteSiteByExternalReferenceCodePageTemplateByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodePageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"patchSiteByExternalReferenceCodePageTemplateByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"putSiteByExternalReferenceCodePageTemplateByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"postSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"putSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteByExternalReferenceCodePageTemplateSetsPageExportBatch",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"postSiteByExternalReferenceCodePageTemplateSetsPageExportBatch"));
					put(
						"mutation#createSiteByExternalReferenceCodePageTemplateSet",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"postSiteByExternalReferenceCodePageTemplateSet"));
					put(
						"mutation#createSiteByExternalReferenceCodePageTemplateSetBatch",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"postSiteByExternalReferenceCodePageTemplateSetBatch"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageTemplateSetPermissionsPage",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"putSiteByExternalReferenceCodePageTemplateSetPermissionsPage"));
					put(
						"mutation#deleteSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"deleteSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"patchSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"putSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"putSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteByExternalReferenceCodeSitePagesPageExportBatch",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"postSiteByExternalReferenceCodeSitePagesPageExportBatch"));
					put(
						"mutation#createSiteByExternalReferenceCodeSitePage",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"postSiteByExternalReferenceCodeSitePage"));
					put(
						"mutation#createSiteByExternalReferenceCodeSitePageBatch",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"postSiteByExternalReferenceCodeSitePageBatch"));
					put(
						"mutation#updateSiteByExternalReferenceCodeSitePagePermissionsPage",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"putSiteByExternalReferenceCodeSitePagePermissionsPage"));
					put(
						"mutation#deleteSiteByExternalReferenceCodeSitePageByExternalReferenceCode",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"deleteSiteByExternalReferenceCodeSitePageByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodeSitePageByExternalReferenceCode",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"patchSiteByExternalReferenceCodeSitePageByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodeSitePageByExternalReferenceCode",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"putSiteByExternalReferenceCodeSitePageByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"postSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSiteByExternalReferenceCodeSitePageByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"putSiteByExternalReferenceCodeSitePageByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteByExternalReferenceCodeUtilityPagesPageExportBatch",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"postSiteByExternalReferenceCodeUtilityPagesPageExportBatch"));
					put(
						"mutation#createSiteByExternalReferenceCodeUtilityPage",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"postSiteByExternalReferenceCodeUtilityPage"));
					put(
						"mutation#createSiteByExternalReferenceCodeUtilityPageBatch",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"postSiteByExternalReferenceCodeUtilityPageBatch"));
					put(
						"mutation#updateSiteByExternalReferenceCodeUtilityPagePermissionsPage",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"putSiteByExternalReferenceCodeUtilityPagePermissionsPage"));
					put(
						"mutation#deleteSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"deleteSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"patchSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"putSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode"));
					put(
						"mutation#createSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecification",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"postSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecification"));
					put(
						"mutation#updateSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePermissionsPage",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"putSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePermissionsPage"));
					put(
						"mutation#createSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstance",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"postSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstance"));
					put(
						"mutation#deleteSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"deleteSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode"));
					put(
						"mutation#patchSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"patchSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode"));
					put(
						"mutation#updateSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"putSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode"));

					put(
						"query#byExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplates",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodeDisplayPageTemplatesPage"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplates",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplatesPage"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplatePermissions",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplatePermissionsPage"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							DisplayPageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePermissionsPage"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplateFolders",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplateFoldersPage"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplateFolderPermissions",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplateFolderPermissionsPage"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							DisplayPageTemplateFolderResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplateFolderByExternalReferenceCodePermissionsPage"));
					put(
						"query#byExternalReferenceCodeFragmentCompositions",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"getSiteByExternalReferenceCodeFragmentCompositionsPage"));
					put(
						"query#byExternalReferenceCodeFragmentCompositionByExternalReferenceCode",
						new ObjectValuePair<>(
							FragmentCompositionResourceImpl.class,
							"getSiteByExternalReferenceCodeFragmentCompositionByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodeFriendlyUrlHistory",
						new ObjectValuePair<>(
							FriendlyUrlHistoryResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodeFriendlyUrlHistory"));
					put(
						"query#byExternalReferenceCodeSitePageByExternalReferenceCodeFriendlyUrlHistory",
						new ObjectValuePair<>(
							FriendlyUrlHistoryResourceImpl.class,
							"getSiteByExternalReferenceCodeSitePageByExternalReferenceCodeFriendlyUrlHistory"));
					put(
						"query#byExternalReferenceCodeUtilityPageByExternalReferenceCodeFriendlyUrlHistory",
						new ObjectValuePair<>(
							FriendlyUrlHistoryResourceImpl.class,
							"getSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodeFriendlyUrlHistory"));
					put(
						"query#byExternalReferenceCodeMasterPages",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"getSiteByExternalReferenceCodeMasterPagesPage"));
					put(
						"query#byExternalReferenceCodeMasterPagePermissions",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"getSiteByExternalReferenceCodeMasterPagePermissionsPage"));
					put(
						"query#byExternalReferenceCodeMasterPageByExternalReferenceCode",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"getSiteByExternalReferenceCodeMasterPageByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodeMasterPageByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							MasterPageResourceImpl.class,
							"getSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePermissionsPage"));
					put(
						"query#byExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElements",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"getSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementsPage"));
					put(
						"query#byExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"getSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodePageElements",
						new ObjectValuePair<>(
							PageElementResourceImpl.class,
							"getSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperienceByExternalReferenceCodePageElementByExternalReferenceCodePageElementsPage"));
					put(
						"query#byExternalReferenceCodePageExperienceByExternalReferenceCode",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"getSiteByExternalReferenceCodePageExperienceByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperiences",
						new ObjectValuePair<>(
							PageExperienceResourceImpl.class,
							"getSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePageExperiencesPage"));
					put(
						"query#byExternalReferenceCodePageExperienceByExternalReferenceCodePageRules",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"getSiteByExternalReferenceCodePageExperienceByExternalReferenceCodePageRulesPage"));
					put(
						"query#byExternalReferenceCodePageRuleByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleResourceImpl.class,
							"getSiteByExternalReferenceCodePageRuleByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodePageRuleActionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"getSiteByExternalReferenceCodePageRuleActionByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodePageRuleByExternalReferenceCodePageRuleActions",
						new ObjectValuePair<>(
							PageRuleActionResourceImpl.class,
							"getSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleActionsPage"));
					put(
						"query#byExternalReferenceCodePageRuleConditionByExternalReferenceCode",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"getSiteByExternalReferenceCodePageRuleConditionByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodePageRuleByExternalReferenceCodePageRuleConditions",
						new ObjectValuePair<>(
							PageRuleConditionResourceImpl.class,
							"getSiteByExternalReferenceCodePageRuleByExternalReferenceCodePageRuleConditionsPage"));
					put(
						"query#byExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#byExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#byExternalReferenceCodePageSpecificationByExternalReferenceCode",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#byExternalReferenceCodeSitePageByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#byExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecifications",
						new ObjectValuePair<>(
							PageSpecificationResourceImpl.class,
							"getSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage"));
					put(
						"query#byExternalReferenceCodePageTemplateSetByExternalReferenceCodePageTemplates",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePageTemplatesPage"));
					put(
						"query#byExternalReferenceCodePageTemplates",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplatesPage"));
					put(
						"query#byExternalReferenceCodePageTemplatePermissions",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplatePermissionsPage"));
					put(
						"query#byExternalReferenceCodePageTemplateByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplateByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodePageTemplateByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							PageTemplateResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePermissionsPage"));
					put(
						"query#byExternalReferenceCodePageTemplateSets",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplateSetsPage"));
					put(
						"query#byExternalReferenceCodePageTemplateSetPermissions",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplateSetPermissionsPage"));
					put(
						"query#byExternalReferenceCodePageTemplateSetByExternalReferenceCode",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodePageTemplateSetByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							PageTemplateSetResourceImpl.class,
							"getSiteByExternalReferenceCodePageTemplateSetByExternalReferenceCodePermissionsPage"));
					put(
						"query#byExternalReferenceCodeSitePages",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"getSiteByExternalReferenceCodeSitePagesPage"));
					put(
						"query#byExternalReferenceCodeSitePagePermissions",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"getSiteByExternalReferenceCodeSitePagePermissionsPage"));
					put(
						"query#byExternalReferenceCodeSitePageByExternalReferenceCode",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"getSiteByExternalReferenceCodeSitePageByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodeSitePageByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							SitePageResourceImpl.class,
							"getSiteByExternalReferenceCodeSitePageByExternalReferenceCodePermissionsPage"));
					put(
						"query#byExternalReferenceCodeUtilityPages",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"getSiteByExternalReferenceCodeUtilityPagesPage"));
					put(
						"query#byExternalReferenceCodeUtilityPagePermissions",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"getSiteByExternalReferenceCodeUtilityPagePermissionsPage"));
					put(
						"query#byExternalReferenceCodeUtilityPageByExternalReferenceCode",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"getSiteByExternalReferenceCodeUtilityPageByExternalReferenceCode"));
					put(
						"query#byExternalReferenceCodeUtilityPageByExternalReferenceCodePermissions",
						new ObjectValuePair<>(
							UtilityPageResourceImpl.class,
							"getSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePermissionsPage"));
					put(
						"query#byExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstances",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"getSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstancesPage"));
					put(
						"query#byExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode",
						new ObjectValuePair<>(
							WidgetPageWidgetInstanceResourceImpl.class,
							"getSiteByExternalReferenceCodeSitePageByExternalReferenceCodeWidgetInstanceByExternalReferenceCode"));
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