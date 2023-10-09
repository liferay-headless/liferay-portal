/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.currency.object.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import com.liferay.object.repository.entity.ObjectEntity;

import java.math.BigDecimal;

import java.util.Locale;
import java.util.Map;

/**
 * @author Riccardo Alberti
 */
@JsonTypeName("C_Currency")
public class CurrencyObjectEntity implements ObjectEntity {

	public String getCode() {
		return _code;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public BigDecimal getExchangeRate() {
		return _exchangeRate;
	}

	public Map<Locale, String> getFormatPatternMap() {
		return _formatPatternMap;
	}

	public long getId() {
		return _id;
	}

	public int getMaximumDecimalPlaces() {
		return _maximumDecimalPlaces;
	}

	public int getMinimumDecimalPlaces() {
		return _minimumDecimalPlaces;
	}

	public Map<Locale, String> getNameMap() {
		return _nameMap;
	}

	public double getPriority() {
		return _priority;
	}

	public String getRoundingMode() {
		return _roundingMode;
	}

	public String getSymbol() {
		return _symbol;
	}

	public long getUserId() {
		return _userId;
	}

	public String getUserName() {
		return _userName;
	}

	public boolean isActive() {
		return _active;
	}

	public boolean isPrimary() {
		return _primary;
	}

	public void setActive(boolean active) {
		_active = active;
	}

	public void setCode(String code) {
		_code = code;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	public void setExchangeRate(BigDecimal exchangeRate) {
		_exchangeRate = exchangeRate;
	}

	public void setFormatPatternMap(Map<Locale, String> formatPatternMap) {
		_formatPatternMap = formatPatternMap;
	}

	public void setId(long id) {
		_id = id;
	}

	public void setMaximumDecimalPlaces(int maximumDecimalPlaces) {
		_maximumDecimalPlaces = maximumDecimalPlaces;
	}

	public void setMinimumDecimalPlaces(int minimumDecimalPlaces) {
		_minimumDecimalPlaces = minimumDecimalPlaces;
	}

	public void setNameMap(Map<Locale, String> nameMap) {
		_nameMap = nameMap;
	}

	public void setPrimary(boolean primary) {
		_primary = primary;
	}

	public void setPriority(double priority) {
		_priority = priority;
	}

	public void setRoundingMode(String roundingMode) {
		_roundingMode = roundingMode;
	}

	public void setSymbol(String symbol) {
		_symbol = symbol;
	}

	public void setUserId(long userId) {
		_userId = userId;
	}

	public void setUserName(String userName) {
		_userName = userName;
	}

	@JsonProperty("active")
	private boolean _active;

	@JsonProperty("code")
	private String _code;

	@JsonProperty("companyId")
	private long _companyId;

	@JsonProperty("exchangeRate")
	private BigDecimal _exchangeRate;

	@JsonProperty("formatPattern_i18n")
	private Map<Locale, String> _formatPatternMap;

	@JsonProperty("c_currencyId")
	private long _id;

	@JsonProperty("maximumDecimalPlaces")
	private int _maximumDecimalPlaces;

	@JsonProperty("minimumDecimalPlaces")
	private int _minimumDecimalPlaces;

	@JsonProperty("name_i18n")
	private Map<Locale, String> _nameMap;

	@JsonProperty("primary")
	private boolean _primary;

	@JsonProperty("priority")
	private double _priority;

	@JsonProperty("roundingMode")
	private String _roundingMode;

	@JsonProperty("symbol")
	private String _symbol;

	@JsonProperty("userId")
	private long _userId;

	@JsonProperty("userName")
	private String _userName;

}