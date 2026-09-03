/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite.internal.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps portal URLs onto the file paths of a static site, and back again as the
 * relative references a browser resolves from a file served off disk.
 *
 * <p>
 * Everything here is pure string handling so that it can be exercised without a
 * portal. The rules it encodes are the ones that break silently when they are
 * wrong: cache busted asset URLs that are not file names, root relative
 * references that only work when the archive lands exactly on the document
 * root, and page paths that have to survive being nested.
 * </p>
 *
 * @author Alejandro Tardín
 */
public class StaticSitePathUtil {

	public static final String ASSETS_DIRECTORY = "_assets";

	public static final String INDEX_FILE_NAME = "index.html";

	public static final List<String> harvestablePathPrefixes = Arrays.asList(
		"/combo", "/documents/", "/image/", "/o/");

	/**
	 * Returns the path of the harvested copy of the resource the given URL
	 * points at, relative to the root of the static site.
	 *
	 * <p>
	 * The query string is dropped, because a file on disk cannot carry one.
	 * When it held nothing but the portal's cache busting parameters that is
	 * lossless. When it held anything else a digest of the surviving parameters
	 * is folded into the file name, so that two genuinely different resources
	 * sharing a path do not overwrite each other.
	 * </p>
	 */
	public static String getAssetPath(String url) {
		String path = _stripHost(url);

		int index = path.indexOf('?');

		if (index == -1) {
			return ASSETS_DIRECTORY + path;
		}

		String query = _stripCacheBustingParameters(path.substring(index + 1));

		path = path.substring(0, index);

		if (query.isEmpty()) {
			return ASSETS_DIRECTORY + path;
		}

		return ASSETS_DIRECTORY +
			_insertBeforeExtension(path, "-" + _digest(query));
	}

	/**
	 * Returns the number of directories the given static site path sits below
	 * the root.
	 */
	public static int getDepth(String path) {
		int depth = 0;

		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) == '/') {
				depth++;
			}
		}

		return depth;
	}

	/**
	 * Returns the path of the file that serves the page with the given friendly
	 * URL.
	 *
	 * <p>
	 * Pages become directories holding an <code>index.html</code> so that
	 * Apache's default <code>DirectoryIndex</code> serves them without any
	 * configuration, and so that nested pages nest on disk the way they nest in
	 * their friendly URLs.
	 * </p>
	 */
	public static String getPagePath(String friendlyURL) {
		String path = _trimSlashes(friendlyURL);

		if (path.isEmpty()) {
			return INDEX_FILE_NAME;
		}

		return path + "/" + INDEX_FILE_NAME;
	}

	/**
	 * Returns the prefix that walks back up to the root of the static site from
	 * the given depth.
	 */
	public static String getRelativePrefix(int depth) {
		StringBuilder sb = new StringBuilder(3 * depth);

		for (int i = 0; i < depth; i++) {
			sb.append("../");
		}

		return sb.toString();
	}

	/**
	 * Returns the reference to use for a resource at the given static site path
	 * from a page at the given depth.
	 *
	 * <p>
	 * The reference is relative rather than root relative so that the archive
	 * works both when it is unzipped onto the document root and when it is
	 * unzipped into a subdirectory of one.
	 * </p>
	 */
	public static String getRelativeReference(String path, int fromDepth) {
		return getRelativePrefix(fromDepth) + path;
	}

	/**
	 * Returns <code>true</code> if the given URL points at a resource that can
	 * be copied into the static site.
	 */
	public static boolean isHarvestable(String url) {
		if ((url == null) || url.isEmpty()) {
			return false;
		}

		for (String scheme : _unharvestableSchemes) {
			if (url.startsWith(scheme)) {
				return false;
			}
		}

		String path = _stripHost(url);

		if (!path.startsWith("/")) {
			return false;
		}

		for (String prefix : _dynamicPathPrefixes) {
			if (path.startsWith(prefix)) {
				return false;
			}
		}

		for (String prefix : harvestablePathPrefixes) {
			if (path.startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the given URL reduced to a portal path when it is fully qualified
	 * against the given portal URL, and unchanged otherwise.
	 *
	 * <p>
	 * The theme names pages and the site root against the portal host, so those
	 * URLs have to come back to paths before anything can be resolved. A URL
	 * qualified against any other host belongs to someone else and is left
	 * alone.
	 * </p>
	 */
	public static String stripPortalURL(String url, String portalURL) {
		if ((portalURL == null) || portalURL.isEmpty() ||
			!url.startsWith(portalURL)) {

			return url;
		}

		String path = url.substring(portalURL.length());

		if (path.isEmpty()) {
			return "/";
		}

		return path;
	}

	public static String trimTrailingSlash(String s) {
		if ((s != null) && s.endsWith("/")) {
			return s.substring(0, s.length() - 1);
		}

		return s;
	}

	private static String _digest(String s) {
		int hash = s.hashCode();

		return Integer.toHexString(hash & 0xfffffff);
	}

	private static String _insertBeforeExtension(String path, String suffix) {
		int index = path.lastIndexOf('.');

		if (index <= path.lastIndexOf('/')) {
			return path + suffix;
		}

		return path.substring(0, index) + suffix + path.substring(index);
	}

	/**
	 * Drops the parameters the portal appends purely to defeat browser caches.
	 * They carry no identity, so two URLs differing only in these are the same
	 * file.
	 *
	 * @see com.liferay.portal.kernel.theme.ThemeDisplay#getMainCSSURL
	 */
	private static String _stripCacheBustingParameters(String query) {
		Map<String, String> parameters = new LinkedHashMap<>();

		for (String pair : query.split("&")) {
			if (pair.isEmpty()) {
				continue;
			}

			int index = pair.indexOf('=');

			String name = (index == -1) ? pair : pair.substring(0, index);

			if (!_cacheBustingParameterNames.contains(name)) {
				parameters.put(name, pair);
			}
		}

		return String.join("&", parameters.values());
	}

	private static String _stripHost(String url) {
		if (url.startsWith("//")) {
			int index = url.indexOf('/', 2);

			if (index == -1) {
				return "/";
			}

			return url.substring(index);
		}

		int index = url.indexOf("://");

		if (index == -1) {
			return url;
		}

		index = url.indexOf('/', index + 3);

		if (index == -1) {
			return "/";
		}

		return url.substring(index);
	}

	private static String _trimSlashes(String s) {
		int begin = 0;
		int end = s.length();

		while ((begin < end) && (s.charAt(begin) == '/')) {
			begin++;
		}

		while ((end > begin) && (s.charAt(end - 1) == '/')) {
			end--;
		}

		return s.substring(begin, end);
	}

	private static final List<String> _cacheBustingParameterNames =
		Arrays.asList(
			"b", "browserId", "colorSchemeId", "languageId", "minifierType",
			"t", "themeId");

	/**
	 * Paths served by the portal itself rather than by a file. Nothing behind
	 * these can be made static.
	 *
	 * @see com.liferay.portal.kernel.util.Portal#PATH_MAIN
	 */
	private static final List<String> _dynamicPathPrefixes = Arrays.asList(
		"/api/", "/c/", "/o/headless-", "/o/oauth2/");

	private static final List<String> _unharvestableSchemes = Arrays.asList(
		"#", "data:", "javascript:", "mailto:", "tel:");

}