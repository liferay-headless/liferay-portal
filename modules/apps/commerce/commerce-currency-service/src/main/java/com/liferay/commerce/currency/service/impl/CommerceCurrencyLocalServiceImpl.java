/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.currency.service.impl;

import com.liferay.commerce.currency.configuration.CommerceCurrencyConfiguration;
import com.liferay.commerce.currency.configuration.RoundingTypeConfiguration;
import com.liferay.commerce.currency.constants.CommerceCurrencyConstants;
import com.liferay.commerce.currency.constants.CommerceCurrencyExchangeRateConstants;
import com.liferay.commerce.currency.constants.CurrencyRepositoryConstants;
import com.liferay.commerce.currency.constants.RoundingTypeConstants;
import com.liferay.commerce.currency.exception.CommerceCurrencyCodeException;
import com.liferay.commerce.currency.exception.CommerceCurrencyNameException;
import com.liferay.commerce.currency.exception.NoSuchCurrencyException;
import com.liferay.commerce.currency.internal.model.listener.PortalInstanceLifecycleListenerImpl;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.model.impl.CommerceCurrencyImpl;
import com.liferay.commerce.currency.object.entity.CurrencyObjectEntity;
import com.liferay.commerce.currency.service.base.CommerceCurrencyLocalServiceBaseImpl;
import com.liferay.commerce.currency.util.ExchangeRateProvider;
import com.liferay.commerce.currency.util.ExchangeRateProviderRegistry;
import com.liferay.object.repository.ObjectRepository;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.SystemSettingsLocator;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Di Giorgi
 * @author Marco Leo
 * @author Alessio Antonio Rendina
 */
@Component(
	property = "model.class.name=com.liferay.commerce.currency.model.CommerceCurrency",
	service = AopService.class
)
public class CommerceCurrencyLocalServiceImpl
	extends CommerceCurrencyLocalServiceBaseImpl {

	@Override
	public CommerceCurrency addCommerceCurrency(
			long userId, String code, Map<Locale, String> nameMap,
			String symbol, BigDecimal rate,
			Map<Locale, String> formatPatternMap, int maxFractionDigits,
			int minFractionDigits, String roundingMode, boolean primary,
			double priority, boolean active)
		throws PortalException {

		User user = _userLocalService.getUser(userId);

		if (primary) {
			rate = BigDecimal.ONE;
		}

		_validate(0, user.getCompanyId(), code, nameMap, primary);

		if (formatPatternMap.isEmpty()) {
			formatPatternMap.put(
				user.getLocale(),
				CommerceCurrencyConstants.DECIMAL_FORMAT_PATTERN);
		}

		if (Validator.isNull(roundingMode)) {
			RoundingTypeConfiguration roundingTypeConfiguration =
				_configurationProvider.getConfiguration(
					RoundingTypeConfiguration.class,
					new SystemSettingsLocator(
						RoundingTypeConstants.SERVICE_NAME));

			RoundingMode roundingModeEnum =
				roundingTypeConfiguration.roundingMode();

			roundingMode = roundingModeEnum.name();
		}

		CurrencyObjectEntity currencyObjectEntity = new CurrencyObjectEntity();

		currencyObjectEntity.setCompanyId(user.getCompanyId());
		currencyObjectEntity.setUserId(user.getUserId());
		currencyObjectEntity.setUserName(user.getFullName());
		currencyObjectEntity.setCode(code);
		currencyObjectEntity.setNameMap(nameMap);
		currencyObjectEntity.setSymbol(symbol);
		currencyObjectEntity.setExchangeRate(rate);
		currencyObjectEntity.setFormatPatternMap(formatPatternMap);
		currencyObjectEntity.setMaximumDecimalPlaces(maxFractionDigits);
		currencyObjectEntity.setMinimumDecimalPlaces(minFractionDigits);
		currencyObjectEntity.setRoundingMode(
			StringUtil.toLowerCase(roundingMode));
		currencyObjectEntity.setPrimary(primary);
		currencyObjectEntity.setPriority(priority);
		currencyObjectEntity.setActive(active);

		return _toCommerceCurrency(
			_currencyObjectRepository.saveObjectEntity(
				0, user.getCompanyId(), userId, currencyObjectEntity,
				new ServiceContext()));
	}

	@Override
	public void deleteCommerceCurrencies(long companyId) {
		try {
			List<CurrencyObjectEntity> currencyObjectEntities =
				_currencyObjectRepository.getObjectEntities(
					0, companyId, 0, null, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null);

			for (CurrencyObjectEntity currencyObjectEntity :
					currencyObjectEntities) {

				_currencyObjectRepository.deleteObjectEntity(
					currencyObjectEntity.getId());
			}
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	@Override
	@SystemEvent(type = SystemEventConstants.TYPE_DELETE)
	public CommerceCurrency deleteCommerceCurrency(
		CommerceCurrency commerceCurrency) {

		try {
			return _toCommerceCurrency(
				_currencyObjectRepository.deleteObjectEntity(
					commerceCurrency.getCommerceCurrencyId()));
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	@Override
	public CommerceCurrency deleteCommerceCurrency(long commerceCurrencyId)
		throws PortalException {

		try {
			return _toCommerceCurrency(
				_currencyObjectRepository.deleteObjectEntity(
					commerceCurrencyId));
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	@Override
	public CommerceCurrency fetchCommerceCurrency(long commerceCurrencyId)
		throws PortalException {

		return _toCommerceCurrency(
			_currencyObjectRepository.fetchObjectEntity(commerceCurrencyId));
	}

	@Override
	public CommerceCurrency fetchPrimaryCommerceCurrency(long companyId) {
		try {
			List<CurrencyObjectEntity> currencyObjectEntities =
				_currencyObjectRepository.getObjectEntities(
					0, companyId, 0, null, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					CurrencyRepositoryConstants.FIND_BY_PRIMARY, true);

			if (currencyObjectEntities.isEmpty()) {
				return null;
			}

			return _toCommerceCurrency(currencyObjectEntities.get(0));
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	@Override
	public List<CommerceCurrency> getCommerceCurrencies(
		long companyId, boolean active) {

		try {
			List<CurrencyObjectEntity> currencyObjectEntities =
				_currencyObjectRepository.getObjectEntities(
					0, companyId, 0, null, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					CurrencyRepositoryConstants.FIND_BY_ACTIVE, active);

			return _toCommerceCurrencies(currencyObjectEntities);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	@Override
	public List<CommerceCurrency> getCommerceCurrencies(
		long companyId, boolean active, int start, int end,
		OrderByComparator<CommerceCurrency> orderByComparator) {

		try {
			List<CurrencyObjectEntity> currencyObjectEntities =
				_currencyObjectRepository.getObjectEntities(
					0, companyId, 0, null, start, end,
					CurrencyRepositoryConstants.FIND_BY_ACTIVE, active);

			return _toCommerceCurrencies(currencyObjectEntities);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	@Override
	public List<CommerceCurrency> getCommerceCurrencies(
		long companyId, int start, int end,
		OrderByComparator<CommerceCurrency> orderByComparator) {

		try {
			List<CurrencyObjectEntity> currencyObjectEntities =
				_currencyObjectRepository.getObjectEntities(
					0, companyId, 0, null, start, end, null);

			return _toCommerceCurrencies(currencyObjectEntities);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	@Override
	public int getCommerceCurrenciesCount(long companyId) {
		try {
			return _currencyObjectRepository.getObjectEntitiesCount(
				0, companyId, 0, null, null);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	@Override
	public int getCommerceCurrenciesCount(long companyId, boolean active) {
		try {
			return _currencyObjectRepository.getObjectEntitiesCount(
				0, companyId, 0, null,
				CurrencyRepositoryConstants.FIND_BY_ACTIVE, active);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	@Override
	public CommerceCurrency getCommerceCurrency(long commerceCurrencyId)
		throws PortalException {

		return _toCommerceCurrency(
			_currencyObjectRepository.getObjectEntity(commerceCurrencyId));
	}

	@Override
	public CommerceCurrency getCommerceCurrency(long companyId, String code)
		throws NoSuchCurrencyException {

		try {
			List<CurrencyObjectEntity> currencyObjectEntities =
				_currencyObjectRepository.getObjectEntities(
					0, companyId, 0, null, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					CurrencyRepositoryConstants.FIND_BY_CODE, code);

			if (currencyObjectEntities.isEmpty()) {
				throw new NoSuchCurrencyException();
			}

			return _toCommerceCurrency(currencyObjectEntities.get(0));
		}
		catch (PortalException portalException) {
			throw new NoSuchCurrencyException(portalException);
		}
	}

	@Override
	public void importDefaultValues(
			boolean updateExchangeRate, ServiceContext serviceContext)
		throws Exception {

		Class<?> clazz = getClass();

		String currenciesPath =
			"com/liferay/commerce/currency/service/impl/dependencies" +
				"/currencies.json";

		String countriesJSON = StringUtil.read(
			clazz.getClassLoader(), currenciesPath, false);

		JSONArray jsonArray = _jsonFactory.createJSONArray(countriesJSON);

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			String code = jsonObject.getString("code");

			List<CurrencyObjectEntity> currencyObjectEntities =
				_currencyObjectRepository.getObjectEntities(
					0, serviceContext.getCompanyId(), 0, null,
					QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					CurrencyRepositoryConstants.FIND_BY_CODE, code);

			CommerceCurrency commerceCurrency = null;

			if (!currencyObjectEntities.isEmpty()) {
				commerceCurrency = _toCommerceCurrency(
					currencyObjectEntities.get(0));
			}

			if (commerceCurrency == null) {
				boolean primary = jsonObject.getBoolean("primary");
				double priority = jsonObject.getDouble("priority");
				double rate = jsonObject.getDouble("rate");
				String symbol = jsonObject.getString("symbol");

				RoundingTypeConfiguration roundingTypeConfiguration =
					_configurationProvider.getConfiguration(
						RoundingTypeConfiguration.class,
						new SystemSettingsLocator(
							RoundingTypeConstants.SERVICE_NAME));

				Map<Locale, String> nameMap = HashMapBuilder.put(
					serviceContext.getLocale(), jsonObject.getString("name")
				).build();

				Map<Locale, String> formatPatternMap = HashMapBuilder.put(
					serviceContext.getLocale(),
					StringBundler.concat(
						symbol, StringPool.SPACE,
						CommerceCurrencyConstants.DECIMAL_FORMAT_PATTERN)
				).build();

				RoundingMode roundingMode =
					roundingTypeConfiguration.roundingMode();

				commerceCurrencyLocalService.addCommerceCurrency(
					serviceContext.getUserId(), code, nameMap, symbol,
					BigDecimal.valueOf(rate), formatPatternMap,
					roundingTypeConfiguration.maximumFractionDigits(),
					roundingTypeConfiguration.minimumFractionDigits(),
					StringUtil.toLowerCase(roundingMode.name()), primary,
					priority, true);
			}
		}

		if (updateExchangeRate) {
			for (String exchangeRateProviderKey :
					_exchangeRateProviderRegistry.
						getExchangeRateProviderKeys()) {

				_updateExchangeRates(
					serviceContext.getCompanyId(), exchangeRateProviderKey);

				break;
			}
		}
	}

	@Override
	public CommerceCurrency setActive(long commerceCurrencyId, boolean active)
		throws PortalException {

		CurrencyObjectEntity currencyObjectEntity =
			_currencyObjectRepository.getObjectEntity(commerceCurrencyId);

		currencyObjectEntity.setActive(active);

		return _toCommerceCurrency(
			_currencyObjectRepository.updateObjectEntity(
				currencyObjectEntity.getUserId(), currencyObjectEntity.getId(),
				currencyObjectEntity, new ServiceContext()));
	}

	@Override
	public void setAopProxy(Object aopProxy) {
		super.setAopProxy(aopProxy);

		Bundle bundle = FrameworkUtil.getBundle(getClass());

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceRegistration = bundleContext.registerService(
			PortalInstanceLifecycleListener.class,
			new PortalInstanceLifecycleListenerImpl(
				commerceCurrencyLocalService),
			null);
	}

	@Override
	public CommerceCurrency setPrimary(long commerceCurrencyId, boolean primary)
		throws PortalException {

		CurrencyObjectEntity currencyObjectEntity =
			_currencyObjectRepository.getObjectEntity(commerceCurrencyId);

		_validate(
			commerceCurrencyId, currencyObjectEntity.getCompanyId(),
			currencyObjectEntity.getCode(), currencyObjectEntity.getNameMap(),
			primary);

		currencyObjectEntity.setPrimary(primary);

		return _toCommerceCurrency(
			_currencyObjectRepository.updateObjectEntity(
				currencyObjectEntity.getUserId(), currencyObjectEntity.getId(),
				currencyObjectEntity, new ServiceContext()));
	}

	@Override
	public CommerceCurrency updateCommerceCurrency(
			long commerceCurrencyId, Map<Locale, String> nameMap, String symbol,
			BigDecimal rate, Map<Locale, String> formatPatternMap,
			int maxFractionDigits, int minFractionDigits, String roundingMode,
			boolean primary, double priority, boolean active,
			ServiceContext serviceContext)
		throws PortalException {

		CurrencyObjectEntity currencyObjectEntity =
			_currencyObjectRepository.getObjectEntity(commerceCurrencyId);

		if (primary) {
			rate = BigDecimal.ONE;
		}

		_validate(
			currencyObjectEntity.getId(), serviceContext.getCompanyId(),
			currencyObjectEntity.getCode(), currencyObjectEntity.getNameMap(),
			primary);

		if (formatPatternMap.isEmpty()) {
			formatPatternMap.put(
				serviceContext.getLocale(),
				CommerceCurrencyConstants.DECIMAL_FORMAT_PATTERN);
		}

		if (Validator.isNull(roundingMode)) {
			RoundingTypeConfiguration roundingTypeConfiguration =
				_configurationProvider.getConfiguration(
					RoundingTypeConfiguration.class,
					new SystemSettingsLocator(
						RoundingTypeConstants.SERVICE_NAME));

			RoundingMode roundingModeEnum =
				roundingTypeConfiguration.roundingMode();

			roundingMode = roundingModeEnum.name();
		}

		currencyObjectEntity.setNameMap(nameMap);
		currencyObjectEntity.setSymbol(symbol);
		currencyObjectEntity.setExchangeRate(rate);
		currencyObjectEntity.setFormatPatternMap(formatPatternMap);
		currencyObjectEntity.setMaximumDecimalPlaces(maxFractionDigits);
		currencyObjectEntity.setMinimumDecimalPlaces(minFractionDigits);
		currencyObjectEntity.setRoundingMode(
			StringUtil.toLowerCase(roundingMode));
		currencyObjectEntity.setPrimary(primary);
		currencyObjectEntity.setPriority(priority);
		currencyObjectEntity.setActive(active);

		return _toCommerceCurrency(
			_currencyObjectRepository.updateObjectEntity(
				currencyObjectEntity.getUserId(), currencyObjectEntity.getId(),
				currencyObjectEntity, new ServiceContext()));
	}

	@Override
	public CommerceCurrency updateCommerceCurrencyRate(
			long commerceCurrencyId, BigDecimal rate)
		throws PortalException {

		CurrencyObjectEntity currencyObjectEntity =
			_currencyObjectRepository.getObjectEntity(commerceCurrencyId);

		currencyObjectEntity.setExchangeRate(rate);

		return _toCommerceCurrency(
			_currencyObjectRepository.updateObjectEntity(
				currencyObjectEntity.getUserId(), currencyObjectEntity.getId(),
				currencyObjectEntity, new ServiceContext()));
	}

	@Override
	public void updateExchangeRate(
			long commerceCurrencyId, String exchangeRateProviderKey)
		throws PortalException {

		ExchangeRateProvider exchangeRateProvider =
			_exchangeRateProviderRegistry.getExchangeRateProvider(
				exchangeRateProviderKey);

		if (exchangeRateProvider == null) {
			return;
		}

		CommerceCurrency commerceCurrency = _toCommerceCurrency(
			_currencyObjectRepository.fetchObjectEntity(commerceCurrencyId));

		CommerceCurrency primaryCommerceCurrency =
			commerceCurrencyLocalService.fetchPrimaryCommerceCurrency(
				commerceCurrency.getCompanyId());

		if (primaryCommerceCurrency == null) {
			return;
		}

		BigDecimal exchangeRate = BigDecimal.ZERO;

		try {
			exchangeRate = exchangeRateProvider.getExchangeRate(
				primaryCommerceCurrency, commerceCurrency);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return;
		}

		commerceCurrencyLocalService.updateCommerceCurrencyRate(
			commerceCurrency.getCommerceCurrencyId(), exchangeRate);
	}

	@Override
	public void updateExchangeRates() throws PortalException {
		_companyLocalService.forEachCompanyId(
			companyId -> {
				CommerceCurrencyConfiguration commerceCurrencyConfiguration =
					_configurationProvider.getConfiguration(
						CommerceCurrencyConfiguration.class,
						new CompanyServiceSettingsLocator(
							companyId,
							CommerceCurrencyExchangeRateConstants.
								SERVICE_NAME));

				if (commerceCurrencyConfiguration.enableAutoUpdate()) {
					String defaultExchangeRateProviderKey =
						commerceCurrencyConfiguration.
							defaultExchangeRateProviderKey();

					_updateExchangeRates(
						companyId, defaultExchangeRateProviderKey);
				}
			});
	}

	@Deactivate
	@Override
	protected void deactivate() {
		super.deactivate();

		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
		}
	}

	private List<CommerceCurrency> _toCommerceCurrencies(
		List<CurrencyObjectEntity> currencyObjectEntities) {

		return TransformUtil.transform(
			currencyObjectEntities, this::_toCommerceCurrency);
	}

	private CommerceCurrency _toCommerceCurrency(
			CurrencyObjectEntity currencyObjectEntity)
		throws PortalException {

		if (currencyObjectEntity == null) {
			return null;
		}

		CommerceCurrency commerceCurrency = new CommerceCurrencyImpl();

		commerceCurrency.setCommerceCurrencyId(currencyObjectEntity.getId());

		commerceCurrency.setCompanyId(currencyObjectEntity.getCompanyId());
		commerceCurrency.setUserId(currencyObjectEntity.getUserId());
		commerceCurrency.setUserName(currencyObjectEntity.getUserName());
		commerceCurrency.setCode(currencyObjectEntity.getCode());
		commerceCurrency.setNameMap(currencyObjectEntity.getNameMap());
		commerceCurrency.setFormatPatternMap(
			currencyObjectEntity.getFormatPatternMap());
		commerceCurrency.setSymbol(currencyObjectEntity.getSymbol());
		commerceCurrency.setRate(currencyObjectEntity.getExchangeRate());
		commerceCurrency.setMaxFractionDigits(
			currencyObjectEntity.getMaximumDecimalPlaces());
		commerceCurrency.setMinFractionDigits(
			currencyObjectEntity.getMinimumDecimalPlaces());
		commerceCurrency.setRoundingMode(
			currencyObjectEntity.getRoundingMode());
		commerceCurrency.setPrimary(currencyObjectEntity.isPrimary());
		commerceCurrency.setPriority(currencyObjectEntity.getPriority());
		commerceCurrency.setActive(currencyObjectEntity.isActive());

		return commerceCurrency;
	}

	private void _updateExchangeRates(
			long companyId, String exchangeRateProviderKey)
		throws PortalException {

		List<CommerceCurrency> commerceCurrencies =
			commerceCurrencyLocalService.getCommerceCurrencies(companyId, true);

		for (CommerceCurrency commerceCurrency : commerceCurrencies) {
			commerceCurrencyLocalService.updateExchangeRate(
				commerceCurrency.getCommerceCurrencyId(),
				exchangeRateProviderKey);
		}
	}

	private void _validate(
			long commerceCurrencyId, long companyId, String code,
			Map<Locale, String> nameMap, boolean primary)
		throws PortalException {

		if (Validator.isNull(code)) {
			throw new CommerceCurrencyCodeException();
		}

		String name = nameMap.get(LocaleUtil.getSiteDefault());

		if (Validator.isNull(name)) {
			throw new CommerceCurrencyNameException();
		}

		if (primary) {
			List<CommerceCurrency> commerceCurrencies = _toCommerceCurrencies(
				_currencyObjectRepository.getObjectEntities(
					0, companyId, 0, null, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					CurrencyRepositoryConstants.FIND_BY_PRIMARY, true));

			for (CommerceCurrency commerceCurrency : commerceCurrencies) {
				if (commerceCurrency.getCommerceCurrencyId() !=
						commerceCurrencyId) {

					commerceCurrency.setPrimary(false);

					commerceCurrencyLocalService.updateCommerceCurrency(
						commerceCurrencyId, commerceCurrency.getNameMap(),
						commerceCurrency.getSymbol(),
						commerceCurrency.getRate(),
						commerceCurrency.getFormatPatternMap(),
						commerceCurrency.getMaxFractionDigits(),
						commerceCurrency.getMinFractionDigits(),
						commerceCurrency.getRoundingMode(),
						commerceCurrency.isPrimary(),
						commerceCurrency.getPriority(),
						commerceCurrency.isActive(), new ServiceContext());
				}
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceCurrencyLocalServiceImpl.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private ObjectRepository<CurrencyObjectEntity> _currencyObjectRepository;

	@Reference
	private ExchangeRateProviderRegistry _exchangeRateProviderRegistry;

	@Reference
	private JSONFactory _jsonFactory;

	private ServiceRegistration<?> _serviceRegistration;

	@Reference
	private UserLocalService _userLocalService;

}