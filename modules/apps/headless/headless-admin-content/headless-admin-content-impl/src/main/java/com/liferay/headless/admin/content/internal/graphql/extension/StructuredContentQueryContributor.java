package com.liferay.headless.admin.content.internal.graphql.extension;

import com.liferay.headless.admin.content.dto.v1_0.VersionInformation;
import com.liferay.headless.admin.content.internal.dto.v1_0.util.VersionInformationUtil;
import com.liferay.headless.delivery.dto.v1_0.StructuredContent;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLTypeExtension;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Miguel Barcos
 */
public class StructuredContentQueryContributor {
	@GraphQLTypeExtension(StructuredContent.class)
	public class StructuredContentVersionInformationTypeExtension {
		public StructuredContentVersionInformationTypeExtension(
			StructuredContent structuredContent) {
			this._structuredContent = structuredContent;
		}

		@GraphQLField
		public VersionInformation versionInformation() throws PortalException {
			JournalArticle journalArticle =
				_journalArticleLocalService.getLatestArticle(
					_structuredContent.getId(),
					WorkflowConstants.STATUS_ANY, false);
			return VersionInformationUtil.toVersionInformation(
				_structuredContent.getSiteId(), journalArticle.getStatus(),
				journalArticle.getVersion());
		}

		private final StructuredContent _structuredContent;
	}

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;
}
