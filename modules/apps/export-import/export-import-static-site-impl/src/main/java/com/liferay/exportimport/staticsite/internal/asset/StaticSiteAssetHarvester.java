/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite.internal.asset;

import com.liferay.exportimport.staticsite.internal.util.StaticSitePathUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Walks a rendered page, turns every portal reference in it into a reference
 * that resolves against files on disk, and records what has to be copied
 * alongside the page for those references to resolve.
 *
 * <p>
 * The theme names the same page in several ways: fully qualified against the
 * portal host, prefixed with the site's friendly URL, or bare when the site
 * answers for the whole host. All of them have to land on the one file the
 * export wrote, so the export hands over a table of the paths it knows rather
 * than leaving this to pattern matching.
 * </p>
 *
 * <p>
 * References the portal serves dynamically cannot be made to resolve at all.
 * Those are neutralized rather than left pointing at a host that will not be
 * there, and recorded so that the export can report what it dropped.
 * </p>
 *
 * @author Alejandro Tardín
 */
public class StaticSiteAssetHarvester {

	public StaticSiteAssetHarvester(
		String portalURL, String siteFriendlyURLPath,
		Map<String, String> pagePathsByFriendlyURLPath) {

		_portalURL = portalURL;
		_siteFriendlyURLPath = StaticSitePathUtil.trimTrailingSlash(
			siteFriendlyURLPath);
		_pagePathsByFriendlyURLPath = pagePathsByFriendlyURLPath;
	}

	/**
	 * Returns the assets referenced by every page rewritten so far, as a map of
	 * the URL to fetch to the path to write it at.
	 */
	public Map<String, String> getAssetPaths() {
		return _assetPaths;
	}

	/**
	 * Returns the dynamic references that were neutralized, so that the export
	 * can tell whoever unzips the archive what stopped working.
	 */
	public Set<String> getDroppedURLs() {
		return _droppedURLs;
	}

	public void rewrite(Document document, String pagePath) {
		int depth = StaticSitePathUtil.getDepth(pagePath);

		for (String attributeName : _URL_ATTRIBUTE_NAMES) {
			Elements elements = document.select("[" + attributeName + "]");

			for (Element element : elements) {
				String value = element.attr(attributeName);

				element.attr(attributeName, _rewriteURL(value, depth));
			}
		}

		for (Element element : document.select("[srcset]")) {
			element.attr(
				"srcset", _rewriteSrcSet(element.attr("srcset"), depth));
		}

		for (Element element : document.select("[style]")) {
			element.attr("style", _rewriteCSS(element.attr("style"), depth));
		}

		for (Element element : document.select("style")) {
			element.html(_rewriteCSS(element.html(), depth));
		}
	}

	/**
	 * Returns the static site path of the page the given portal path addresses,
	 * or <code>null</code> when it addresses no exported page.
	 */
	private String _getPagePath(String path) {
		String anchor = "";

		int index = path.indexOf('#');

		if (index != -1) {
			anchor = path.substring(index);

			path = path.substring(0, index);
		}

		index = path.indexOf('?');

		if (index != -1) {
			path = path.substring(0, index);
		}

		path = StaticSitePathUtil.trimTrailingSlash(path);

		// The portal root and the site root both answer for the page the
		// document root was written from

		if (path.isEmpty() || path.equals(_siteFriendlyURLPath)) {
			return StaticSitePathUtil.INDEX_FILE_NAME + anchor;
		}

		if (path.startsWith(_siteFriendlyURLPath + "/")) {
			path = path.substring(_siteFriendlyURLPath.length());
		}

		String pagePath = _pagePathsByFriendlyURLPath.get(path);

		if (pagePath == null) {
			return null;
		}

		return pagePath + anchor;
	}

	private String _rewriteCSS(String css, int depth) {
		Matcher matcher = _cssURLPattern.matcher(css);

		StringBuilder sb = new StringBuilder();

		int end = 0;

		while (matcher.find()) {
			sb.append(css, end, matcher.start(2));
			sb.append(_rewriteURL(matcher.group(2), depth));

			end = matcher.end(2);
		}

		sb.append(css.substring(end));

		return sb.toString();
	}

	private String _rewriteSrcSet(String srcSet, int depth) {
		List<String> candidates = new ArrayList<>();

		for (String candidate : srcSet.split(",")) {
			String trimmedCandidate = candidate.trim();

			if (trimmedCandidate.isEmpty()) {
				continue;
			}

			int index = trimmedCandidate.indexOf(' ');

			if (index == -1) {
				candidates.add(_rewriteURL(trimmedCandidate, depth));
			}
			else {
				candidates.add(
					_rewriteURL(trimmedCandidate.substring(0, index), depth) +
						trimmedCandidate.substring(index));
			}
		}

		return String.join(", ", candidates);
	}

	private String _rewriteURL(String url, int depth) {
		if ((url == null) || url.isEmpty() || url.startsWith("#")) {
			return url;
		}

		// A URL fully qualified against the portal names something the export
		// may well hold, so it is reduced to a path before anything else

		String path = StaticSitePathUtil.stripPortalURL(url, _portalURL);

		String pagePath = _getPagePath(path);

		if (pagePath != null) {
			return StaticSitePathUtil.getRelativeReference(pagePath, depth);
		}

		if (StaticSitePathUtil.isHarvestable(path)) {
			String assetPath = StaticSitePathUtil.getAssetPath(path);

			_assetPaths.put(path, assetPath);

			return StaticSitePathUtil.getRelativeReference(assetPath, depth);
		}

		if (path.startsWith("/")) {

			// Served by the portal, so there is nothing to copy and nowhere for
			// the reference to point once the portal is gone

			_droppedURLs.add(path);

			return "#";
		}

		return url;
	}

	private static final String[] _URL_ATTRIBUTE_NAMES = {
		"action", "data-src", "href", "poster", "src"
	};

	private static final Pattern _cssURLPattern = Pattern.compile(
		"url\\((['\"]?)([^'\")]+)\\1\\)");

	private final Map<String, String> _assetPaths = new LinkedHashMap<>();
	private final Set<String> _droppedURLs = new LinkedHashSet<>();
	private final Map<String, String> _pagePathsByFriendlyURLPath;
	private final String _portalURL;
	private final String _siteFriendlyURLPath;

}