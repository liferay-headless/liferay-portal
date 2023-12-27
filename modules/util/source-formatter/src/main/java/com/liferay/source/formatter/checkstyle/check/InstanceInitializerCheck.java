/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.source.formatter.check.util.BNDSourceUtil;
import com.liferay.source.formatter.check.util.JavaSourceUtil;
import com.liferay.source.formatter.check.util.SourceUtil;
import com.liferay.source.formatter.parser.JavaClass;
import com.liferay.source.formatter.parser.JavaClassParser;
import com.liferay.source.formatter.parser.JavaMethod;
import com.liferay.source.formatter.parser.JavaParameter;
import com.liferay.source.formatter.parser.JavaSignature;
import com.liferay.source.formatter.parser.JavaTerm;
import com.liferay.source.formatter.parser.ParseException;
import com.liferay.source.formatter.util.FileUtil;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alan Huang
 */
public class InstanceInitializerCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.INSTANCE_INIT};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		DetailAST parentDetailAST = detailAST.getParent();

		if (parentDetailAST.getType() != TokenTypes.OBJBLOCK) {
			return;
		}

		parentDetailAST = parentDetailAST.getParent();

		if (parentDetailAST.getType() != TokenTypes.LITERAL_NEW) {
			return;
		}

		DetailAST firstChildDetailAST = detailAST.getFirstChild();

		if (firstChildDetailAST.getType() != TokenTypes.SLIST) {
			return;
		}

		List<DetailAST> exprDetailASTList = getAllChildTokens(
			firstChildDetailAST, false, TokenTypes.EXPR);

		if (exprDetailASTList.size() >= 2) {
			_checkAttributeOrder(exprDetailASTList);
		}

		JavaClass javaClass = null;

		try {
			javaClass = _getJavaClass(detailAST, parentDetailAST);
		}
		catch (IOException | ParseException exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return;
		}

		if (javaClass == null) {
			return;
		}

		_checkSetCall(firstChildDetailAST, exprDetailASTList, javaClass);

		for (DetailAST literalIfDetailAST :
				getAllChildTokens(
					firstChildDetailAST, false, TokenTypes.LITERAL_IF)) {

			_checkIfStatement(literalIfDetailAST, javaClass);
		}
	}

	private void _checkAttributeOrder(List<DetailAST> exprDetailASTList) {
		String previousVariableName = null;
		String previousMethodName = null;

		for (DetailAST exprDetailAST : exprDetailASTList) {
			DetailAST firstChildDetailAST = exprDetailAST.getFirstChild();

			if (firstChildDetailAST.getType() == TokenTypes.ASSIGN) {
				String variableName = getName(firstChildDetailAST);

				if (Validator.isNotNull(
						getTypeName(
							getVariableTypeDetailAST(
								firstChildDetailAST, variableName, false),
							false))) {

					continue;
				}

				if ((previousVariableName != null) &&
					(previousVariableName.compareToIgnoreCase(variableName) >
						0)) {

					log(
						exprDetailAST, _MSG_INCORRECT_ASSIGN_ORDER,
						variableName, previousVariableName,
						firstChildDetailAST.getLineNo());
				}
				else if (Validator.isNotNull(previousMethodName)) {
					log(
						exprDetailAST, _MSG_MOVE_ASSIGN_BEFORE_METHOD_CALL,
						variableName, previousMethodName,
						firstChildDetailAST.getLineNo());
				}

				previousVariableName = variableName;
			}
			else if (firstChildDetailAST.getType() == TokenTypes.METHOD_CALL) {
				String methodName = getName(firstChildDetailAST);

				if (Validator.isNull(methodName) ||
					!methodName.matches("set[A-Z].+")) {

					continue;
				}

				if ((previousMethodName != null) &&
					(previousMethodName.compareToIgnoreCase(methodName) > 0)) {

					log(
						exprDetailAST, _MSG_INCORRECT_METHOD_CALL_ORDER,
						methodName, previousMethodName,
						firstChildDetailAST.getLineNo());
				}

				previousMethodName = methodName;
			}
		}
	}

	private void _checkHasReplacableMethodSignature(
		DetailAST detailAST, String methodName, JavaClass javaClass) {

		JavaMethod javaMethod = _getUnsafeSupplierSetMethod(
			javaClass, methodName);

		if (javaMethod == null) {
			return;
		}

		if (detailAST.getType() == TokenTypes.METHOD_CALL) {
			log(detailAST, _MSG_INLINE_IF_STATEMENT, methodName);
		}
		else {
			JavaParameter javaParameter = _getFirstJavaParameter(javaMethod);

			log(
				detailAST, _MSG_USE_SET_METHOD_INSTEAD, methodName,
				javaParameter.getParameterType());
		}
	}

	private void _checkIfStatement(
		DetailAST literalIfDetailAST, JavaClass javaClass) {

		DetailAST slistDetailAST = literalIfDetailAST.findFirstToken(
			TokenTypes.SLIST);

		List<DetailAST> exprDetailASTList = getAllChildTokens(
			slistDetailAST, false, TokenTypes.EXPR);

		for (DetailAST exprDetailAST : exprDetailASTList) {
			DetailAST firstChildDetailAST = exprDetailAST.getFirstChild();

			if (firstChildDetailAST == null) {
				continue;
			}

			if (firstChildDetailAST.getType() == TokenTypes.ASSIGN) {
				firstChildDetailAST = firstChildDetailAST.getFirstChild();

				if (firstChildDetailAST.getType() != TokenTypes.IDENT) {
					continue;
				}

				String variableName = firstChildDetailAST.getText();

				String methodName =
					"set" + StringUtil.upperCaseFirstLetter(variableName);

				_checkHasReplacableMethodSignature(
					firstChildDetailAST, methodName, javaClass);
			}
			else if (firstChildDetailAST.getType() == TokenTypes.METHOD_CALL) {
				DetailAST dotDetailAST = firstChildDetailAST.findFirstToken(
					TokenTypes.DOT);

				if (dotDetailAST != null) {
					continue;
				}

				String methodName = getMethodName(firstChildDetailAST);

				if (!methodName.startsWith("set")) {
					continue;
				}

				_checkHasReplacableMethodSignature(
					firstChildDetailAST, methodName, javaClass);
			}
		}
	}

	private void _checkSetAssignCall(DetailAST detailAST, JavaClass javaClass) {
		if (_isTestModule(getAbsolutePath())) {
			return;
		}

		String methodName =
			"set" + StringUtil.upperCaseFirstLetter(getName(detailAST));

		JavaMethod javaMethod = _getUnsafeSupplierSetMethod(
			javaClass, methodName);

		if (javaMethod == null) {
			return;
		}

		JavaParameter javaParameter = _getFirstJavaParameter(javaMethod);

		log(
			getStartLineNumber(detailAST), _MSG_USE_UNSAFE_SUPPLIER_SET_INSTEAD,
			methodName,
			javaParameter.getParameterType() + " " +
				javaParameter.getParameterName());
	}

	private void _checkSetCall(
		DetailAST detailAST, List<DetailAST> exprDetailASTList,
		JavaClass javaClass) {

		for (DetailAST exprDetailAST : exprDetailASTList) {
			DetailAST firstChildDetailAST = exprDetailAST.getFirstChild();

			if (firstChildDetailAST.getType() == TokenTypes.ASSIGN) {
				_checkSetAssignCall(firstChildDetailAST, javaClass);
			}
			else if (firstChildDetailAST.getType() == TokenTypes.METHOD_CALL) {
				_checkSetMethodCall(detailAST, firstChildDetailAST, javaClass);
			}
		}
	}

	private void _checkSetMethodCall(
		DetailAST detailAST, DetailAST firstChildDetailAST,
		JavaClass javaClass) {

		DetailAST dotDetailAST = firstChildDetailAST.findFirstToken(
			TokenTypes.DOT);

		if (dotDetailAST != null) {
			return;
		}

		int startLineNumber = getStartLineNumber(firstChildDetailAST);

		String methodName = getMethodName(firstChildDetailAST);

		if (!methodName.matches("set[A-Z]\\w*")) {
			return;
		}

		DetailAST elistDetailAST = firstChildDetailAST.findFirstToken(
			TokenTypes.ELIST);

		firstChildDetailAST = elistDetailAST.getFirstChild();

		if ((firstChildDetailAST == null) ||
			(firstChildDetailAST.getType() != TokenTypes.EXPR)) {

			return;
		}

		DetailAST firstGrandChildDetailAST =
			firstChildDetailAST.getFirstChild();

		if ((firstGrandChildDetailAST != null) &&
			(firstGrandChildDetailAST.getType() == TokenTypes.METHOD_REF)) {

			return;
		}

		String variableName = StringUtil.lowerCaseFirstLetter(
			methodName.substring(3));

		JavaMethod javaMethod = null;

		if (!_isTestModule(getAbsolutePath())) {
			javaMethod = _getUnsafeSupplierSetMethod(javaClass, methodName);
		}

		if (javaMethod != null) {
			JavaParameter javaParameter = _getFirstJavaParameter(javaMethod);

			log(
				getStartLineNumber(detailAST),
				_MSG_USE_UNSAFE_SUPPLIER_SET_INSTEAD, methodName,
				javaParameter.getParameterType() + " " +
					javaParameter.getParameterName());

			return;
		}

		List<String> names = getNames(detailAST, true);

		if (names.contains(variableName)) {
			return;
		}

		Pattern pattern = Pattern.compile(
			"\\s(\\S+)\\s+(\\S+\\.)?" + variableName);

		for (JavaTerm javaTerm : javaClass.getChildJavaTerms()) {
			if (!javaTerm.isJavaVariable() || javaTerm.isPrivate()) {
				continue;
			}

			Matcher matcher = pattern.matcher(javaTerm.getContent());

			if (matcher.find()) {
				log(
					startLineNumber, _MSG_USE_ASSIGN_INSTEAD,
					javaTerm.getName(), methodName);

				break;
			}
		}
	}

	private synchronized Map<String, String> _getBundleSymbolicNamesMap(
		String absolutePath) {

		if (_bundleSymbolicNamesMap == null) {
			_bundleSymbolicNamesMap = BNDSourceUtil.getBundleSymbolicNamesMap(
				_getRootDirName(absolutePath));
		}

		return _bundleSymbolicNamesMap;
	}

	private JavaParameter _getFirstJavaParameter(JavaMethod javaMethod) {
		JavaSignature javaSignature = javaMethod.getSignature();

		if (javaSignature == null) {
			return null;
		}

		List<JavaParameter> javaParameters = javaSignature.getParameters();

		if (ListUtil.isEmpty(javaParameters)) {
			return null;
		}

		return javaParameters.get(0);
	}

	private JavaClass _getJavaClass(
			DetailAST detailAST, DetailAST parentDetailAST)
		throws IOException, ParseException {

		String fullyQualifiedTypeName = null;

		DetailAST firstChildDetailAST = parentDetailAST.getFirstChild();

		if (firstChildDetailAST.getType() == TokenTypes.IDENT) {
			fullyQualifiedTypeName = getFullyQualifiedTypeName(
				firstChildDetailAST.getText(), detailAST, false);
		}
		else if (firstChildDetailAST.getType() == TokenTypes.DOT) {
			FullIdent fullIdent = FullIdent.createFullIdent(
				firstChildDetailAST);

			fullyQualifiedTypeName = fullIdent.getText();
		}

		if (fullyQualifiedTypeName == null) {
			return null;
		}

		String absolutePath = getAbsolutePath();

		File javaFile = JavaSourceUtil.getJavaFile(
			fullyQualifiedTypeName, _getRootDirName(absolutePath),
			_getBundleSymbolicNamesMap(absolutePath));

		if (javaFile == null) {
			return null;
		}

		return JavaClassParser.parseJavaClass(
			SourceUtil.getAbsolutePath(javaFile), FileUtil.read(javaFile));
	}

	private synchronized String _getRootDirName(String absolutePath) {
		if (_rootDirName != null) {
			return _rootDirName;
		}

		_rootDirName = SourceUtil.getRootDirName(absolutePath);

		return _rootDirName;
	}

	private JavaMethod _getUnsafeSupplierSetMethod(
		JavaClass javaClass, String methodName) {

		for (JavaTerm javaTerm : javaClass.getChildJavaTerms()) {
			if (!javaTerm.isJavaMethod()) {
				continue;
			}

			JavaMethod javaMethod = (JavaMethod)javaTerm;

			if (!StringUtil.equals(javaMethod.getName(), methodName)) {
				continue;
			}

			JavaParameter javaParameter = _getFirstJavaParameter(javaMethod);

			if ((javaParameter == null) ||
				!StringUtil.startsWith(
					javaParameter.getParameterType(), "UnsafeSupplier<")) {

				continue;
			}

			return javaMethod;
		}

		return null;
	}

	private boolean _isTestModule(String absolutePath) {
		if (absolutePath.contains("/test/") ||
			absolutePath.contains("/testIntegration/") ||
			absolutePath.endsWith("Test.java")) {

			return true;
		}

		return false;
	}

	private static final String _MSG_INCORRECT_ASSIGN_ORDER =
		"assign.order.incorrect";

	private static final String _MSG_INCORRECT_METHOD_CALL_ORDER =
		"method.call.order.incorrect";

	private static final String _MSG_INLINE_IF_STATEMENT =
		"if.statement.inline";

	private static final String _MSG_MOVE_ASSIGN_BEFORE_METHOD_CALL =
		"assign.move.before.method.call";

	private static final String _MSG_USE_ASSIGN_INSTEAD = "assign.use.instead";

	private static final String _MSG_USE_SET_METHOD_INSTEAD =
		"set.method.use.instead";

	private static final String _MSG_USE_UNSAFE_SUPPLIER_SET_INSTEAD =
		"assign.use.unsafe.supplier.set.instead";

	private static final Log _log = LogFactoryUtil.getLog(
		InstanceInitializerCheck.class);

	private volatile Map<String, String> _bundleSymbolicNamesMap;
	private volatile String _rootDirName;

}