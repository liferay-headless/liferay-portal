/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.web.internal.frontend.data.set.filter;

import com.liferay.frontend.data.set.filter.BaseSelectionFDSFilter;
import com.liferay.frontend.data.set.filter.FDSFilter;
import com.liferay.frontend.data.set.filter.SelectionFDSFilterItem;
import com.liferay.launch.web.internal.constants.LaunchFDSNames;
import com.liferay.portal.kernel.util.ListUtil;

import java.util.List;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jose Luis Navarro
 */
@Component(
	property = "frontend.data.set.name=" + LaunchFDSNames.LAUNCH_SET_ERRORS,
	service = FDSFilter.class
)
public class LaunchSetErrorSeveritySelectionFDSFilter
	extends BaseSelectionFDSFilter {

	@Override
	public String getId() {
		return "severity";
	}

	@Override
	public String getLabel() {
		return "severity";
	}

	@Override
	public List<SelectionFDSFilterItem> getSelectionFDSFilterItems(
		Locale locale) {

		return ListUtil.fromArray(
			new SelectionFDSFilterItem("critical", "critical"),
			new SelectionFDSFilterItem("error", "error"));
	}

}