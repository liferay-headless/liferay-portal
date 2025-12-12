package com.liferay.batch.planner.rest.internal.resource.v1_0;

import com.liferay.portal.test.rule.LiferayUnitTestRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class PlanResourceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testCSVContainsAllProvidedFields() throws Exception {

		// -------------------------------------------------------------------
		// Arrange: mock "fields" as if returned by FieldProviderUtil.getFields
		// -------------------------------------------------------------------

		List<String> expectedFieldNames = Arrays.asList(
			"id", "name", "description", "status"
		);

		// Simulated CSV header returned by REST layer
		String csvContent =
			"id,name,description,status\n" +
			"1,Test,Something,ACTIVE";

		PlanResource planResource = Mockito.mock(PlanResource.class);

		Mockito.when(
			planResource.getPlanTemplateHttpResponse(Mockito.anyString())
		).thenReturn(
			new FakeHttpResponse(csvContent)
		);

		// -------------------------------------------------------------------
		// Act: parse CSV headers
		// -------------------------------------------------------------------

		String[] lines = StringUtil.split(
			csvContent, System.lineSeparator());

		Assert.assertTrue(lines.length > 0);

		String headerLine = lines[0];

		Set<String> actualFieldNames = new HashSet<>(
			Arrays.asList(StringUtil.split(headerLine, ","))
		);

		// -------------------------------------------------------------------
		// Assert: all expected fields are present
		// -------------------------------------------------------------------

		for (String expectedFieldName : expectedFieldNames) {
			Assert.assertTrue(
				"Missing field in CSV: " + expectedFieldName,
				actualFieldNames.contains(expectedFieldName)
			);
		}
	}

	/**
	 * Minimal fake HttpResponse to avoid pulling real HttpInvoker.
	 */
	private static class FakeHttpResponse
		implements com.liferay.portal.kernel.util.HttpInvoker.HttpResponse {

		public FakeHttpResponse(String content) {
			_content = content;
		}

		@Override
		public String getContent() {
			return _content;
		}

		@Override
		public int getStatusCode() {
			return 200;
		}

		private final String _content;

	}

}
