package com.liferay.headless.admin.content.internal.jaxrs.extension;

import com.liferay.headless.admin.content.internal.dto.v1_0.util.VersionInformationUtil;
import com.liferay.headless.delivery.dto.v1_0.StructuredContent;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.jaxrs.context.EntityExtensionContext;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Luis Miguel Barcos
 */
@Component(
	immediate = true,
	service = EntityExtensionContext.class
)
public class StructuredContentExtensionContext
	extends EntityExtensionContext<StructuredContent> {

	public StructuredContentExtensionContext(
		JournalArticleLocalService journalArticleLocalService) {
		this._journalArticleLocalService = journalArticleLocalService;
	}

	@Override
	public Map<String, Object> getEntityExtendedProperties(
		StructuredContent entity) {
		try {
			JournalArticle journalArticle =
				_journalArticleLocalService.getLatestArticle(entity.getId(),
					WorkflowConstants.STATUS_ANY, false);
			return Collections.singletonMap(
				"versionInformation",
				VersionInformationUtil.toVersionInformation(
					journalArticle.getGroupId(), journalArticle.getStatus(),
					journalArticle.getVersion()));
		}
		catch (PortalException e) {
			throw new NotFoundException();
		}
	}

	@Override
	public Set<String> getEntityFilteredPropertyKeys(
		StructuredContent entity) {
		return null;
	}

	private JournalArticleLocalService _journalArticleLocalService;
}
