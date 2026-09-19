/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.source.formatter.check.util.JavaSourceUtil;
import com.liferay.source.formatter.parser.JavaTerm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Adolfo Pérez
 */
public class JavaArquillianStaticInitializerCheck extends BaseJavaTermCheck {

	@Override
	public boolean isLiferaySourceCheck() {
		return true;
	}

	@Override
	protected String doProcess(
		String fileName, String absolutePath, JavaTerm javaTerm,
		String fileContent) {

		String content = javaTerm.getContent();

		if (!javaTerm.isStatic() ||
			!_isArquillianTest(absolutePath, fileContent)) {

			return content;
		}

		int start = _getStaticInitializerValueStartPos(content, javaTerm);

		if (start == -1) {
			return content;
		}

		String staticInitializerValueContent =
			_getStaticInitializerValueContent(content, start);

		Matcher matcher = _unsafeCallPattern.matcher(
			staticInitializerValueContent);

		while (matcher.find()) {
			_addMessage(
				fileName, javaTerm,
				StringBundler.concat(
					"Do not use \"", matcher.group(1), "\" in a static ",
					"initializer of an Arquillian test"),
				matcher.start());
		}

		matcher = _randomStringPattern.matcher(staticInitializerValueContent);

		while (matcher.find()) {
			Matcher randomizerBumperMatcher = _randomizerBumperPattern.matcher(
				JavaSourceUtil.getMethodCall(
					staticInitializerValueContent, matcher.start()));

			if (randomizerBumperMatcher.find()) {
				_addMessage(
					fileName, javaTerm,
					StringBundler.concat(
						"Do not use \"", matcher.group(1), "\" with \"",
						randomizerBumperMatcher.group(1),
						"\" in a static initializer of an Arquillian test"),
					matcher.start());
			}
		}

		return content;
	}

	@Override
	protected String[] getCheckableJavaTermNames() {
		return new String[] {JAVA_STATIC_BLOCK, JAVA_VARIABLE};
	}

	private void _addMessage(
		String fileName, JavaTerm javaTerm, String message, int pos) {

		addMessage(
			fileName,
			StringBundler.concat(
				message, ", since it needs the portal but static initializers ",
				"also run in the client JVM. Initialize it in a @Before or ",
				"@BeforeClass method or where it is used"),
			javaTerm.getLineNumber(pos));
	}

	private int _getAnonymousClassEndPos(String content, int pos) {
		int i = _skipWhitespace(content, pos);

		int start = i;

		while ((i < content.length()) &&
			   (Character.isJavaIdentifierPart(content.charAt(i)) ||
				(content.charAt(i) == CharPool.PERIOD))) {

			i++;
		}

		if (i == start) {
			return -1;
		}

		i = _skipWhitespace(content, i);

		if ((i < content.length()) &&
			(content.charAt(i) == CharPool.LESS_THAN)) {

			i = _getClosingPos(
				content, i, CharPool.LESS_THAN, CharPool.GREATER_THAN);

			i = _skipWhitespace(content, i + 1);
		}

		if ((i >= content.length()) ||
			(content.charAt(i) != CharPool.OPEN_PARENTHESIS)) {

			return -1;
		}

		i = _getClosingPos(
			content, i, CharPool.OPEN_PARENTHESIS, CharPool.CLOSE_PARENTHESIS);

		i = _skipWhitespace(content, i + 1);

		if ((i >= content.length()) ||
			(content.charAt(i) != CharPool.OPEN_CURLY_BRACE)) {

			return -1;
		}

		int end = _getClosingPos(
			content, i, CharPool.OPEN_CURLY_BRACE, CharPool.CLOSE_CURLY_BRACE);

		return end + 1;
	}

	private int _getClosingPos(String content, int pos, char open, char close) {
		int level = 0;

		for (int i = pos; i < content.length(); i++) {
			char c = content.charAt(i);

			if ((c == CharPool.QUOTE) || (c == CharPool.APOSTROPHE)) {
				i = _getLiteralEndPos(content, i);

				continue;
			}

			if (c == open) {
				level++;
			}
			else if (c == close) {
				level--;

				if (level == 0) {
					return i;
				}
			}
		}

		return content.length() - 1;
	}

	private int _getExpressionEndPos(String content, int pos) {
		int level = 0;

		for (int i = pos; i < content.length(); i++) {
			char c = content.charAt(i);

			if ((c == CharPool.QUOTE) || (c == CharPool.APOSTROPHE)) {
				i = _getLiteralEndPos(content, i);

				continue;
			}

			if ((c == CharPool.OPEN_BRACKET) ||
				(c == CharPool.OPEN_CURLY_BRACE) ||
				(c == CharPool.OPEN_PARENTHESIS)) {

				level++;
			}
			else if ((c == CharPool.CLOSE_BRACKET) ||
					 (c == CharPool.CLOSE_CURLY_BRACE) ||
					 (c == CharPool.CLOSE_PARENTHESIS)) {

				if (level == 0) {
					return i;
				}

				level--;
			}
			else if (((c == CharPool.COMMA) || (c == CharPool.SEMICOLON)) &&
					 (level == 0)) {

				return i;
			}
		}

		return content.length();
	}

	private int _getLambdaBodyEndPos(String content, int pos) {
		int i = _skipWhitespace(content, pos);

		if ((i < content.length()) &&
			(content.charAt(i) == CharPool.OPEN_CURLY_BRACE)) {

			int end = _getClosingPos(
				content, i, CharPool.OPEN_CURLY_BRACE,
				CharPool.CLOSE_CURLY_BRACE);

			return end + 1;
		}

		return _getExpressionEndPos(content, i);
	}

	private int _getLiteralEndPos(String content, int pos) {
		char delimiter = content.charAt(pos);

		for (int i = pos + 1; i < content.length(); i++) {
			char c = content.charAt(i);

			if (c == CharPool.BACK_SLASH) {
				i++;
			}
			else if (c == delimiter) {
				return i;
			}
		}

		return content.length() - 1;
	}

	private String _getStaticInitializerValueContent(
		String content, int start) {

		StringBuilder sb = new StringBuilder(content);

		_maskRange(sb, 0, start);

		int i = start;

		while (i < content.length()) {
			char c = content.charAt(i);

			if ((c == CharPool.QUOTE) || (c == CharPool.APOSTROPHE)) {
				int end = _getLiteralEndPos(content, i);

				_maskRange(sb, i + 1, end);

				i = end + 1;
			}
			else if (content.startsWith("//", i)) {
				int end = content.indexOf(CharPool.NEW_LINE, i);

				if (end == -1) {
					end = content.length();
				}

				_maskRange(sb, i, end);

				i = end;
			}
			else if (content.startsWith("/*", i)) {
				int end = content.indexOf("*/", i);

				if (end == -1) {
					end = content.length();
				}
				else {
					end = end + 2;
				}

				_maskRange(sb, i, end);

				i = end;
			}
			else if (content.startsWith("->", i)) {
				int end = _getLambdaBodyEndPos(content, i + 2);

				_maskRange(sb, i, end);

				i = end;
			}
			else if (_isKeyword(content, "new", i)) {
				int end = _getAnonymousClassEndPos(content, i + 3);

				if (end == -1) {
					i += 3;
				}
				else {
					_maskRange(sb, i, end);

					i = end;
				}
			}
			else {
				i++;
			}
		}

		return sb.toString();
	}

	private int _getStaticInitializerValueStartPos(
		String content, JavaTerm javaTerm) {

		if (!javaTerm.isJavaVariable()) {
			return 0;
		}

		int level = 0;

		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);

			if ((c == CharPool.QUOTE) || (c == CharPool.APOSTROPHE)) {
				i = _getLiteralEndPos(content, i);

				continue;
			}

			if (content.startsWith("//", i)) {
				i = content.indexOf(CharPool.NEW_LINE, i);

				if (i == -1) {
					return -1;
				}

				continue;
			}

			if (content.startsWith("/*", i)) {
				i = content.indexOf("*/", i);

				if (i == -1) {
					return -1;
				}

				i++;

				continue;
			}

			if (c == CharPool.OPEN_PARENTHESIS) {
				level++;
			}
			else if (c == CharPool.CLOSE_PARENTHESIS) {
				level--;
			}
			else if ((c == CharPool.EQUAL) && (level == 0)) {
				return i + 1;
			}
		}

		return -1;
	}

	private boolean _isArquillianTest(String absolutePath, String fileContent) {
		if (absolutePath.contains("/src/testIntegration/java/") ||
			absolutePath.contains("/test/integration/") ||
			fileContent.contains("@RunWith(Arquillian.class)")) {

			return true;
		}

		return false;
	}

	private boolean _isKeyword(String content, String keyword, int pos) {
		if (!content.startsWith(keyword, pos) ||
			((pos > 0) &&
			 Character.isJavaIdentifierPart(content.charAt(pos - 1)))) {

			return false;
		}

		int end = pos + keyword.length();

		if ((end < content.length()) &&
			Character.isJavaIdentifierPart(content.charAt(end))) {

			return false;
		}

		return true;
	}

	private void _maskRange(StringBuilder sb, int start, int end) {
		for (int i = start; i < end; i++) {
			if (sb.charAt(i) != CharPool.NEW_LINE) {
				sb.setCharAt(i, CharPool.SPACE);
			}
		}
	}

	private int _skipWhitespace(String content, int pos) {
		while ((pos < content.length()) &&
			   Character.isWhitespace(content.charAt(pos))) {

			pos++;
		}

		return pos;
	}

	private static final Pattern _randomizerBumperPattern = Pattern.compile(
		"\\b(BBCodeRandomizerBumper|LayoutFriendlyURLRandomizerBumper|" +
			"SiteFriendlyURLKeywordRandomizerBumper|TikaRandomizerBumper)\\b");
	private static final Pattern _randomStringPattern = Pattern.compile(
		"\\b(RandomTestUtil\\.randomStrings?)\\(");
	private static final Pattern _unsafeCallPattern = Pattern.compile(
		StringBundler.concat(
			"\\b((?:\\w+ServiceUtil|ServiceContextTestUtil|TestPropsValues)\\.",
			"\\w+|\\w+TestUtil\\.(?:add|delete|update)\\w*|",
			"DLTestUtil\\.randomTextFileBytes|",
			"RandomTestUtil\\.next(?:Double|Int|Long))\\("));

}