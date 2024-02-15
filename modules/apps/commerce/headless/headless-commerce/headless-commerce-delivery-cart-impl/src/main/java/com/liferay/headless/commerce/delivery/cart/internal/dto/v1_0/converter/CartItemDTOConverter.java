/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.cart.internal.dto.v1_0.converter;

import com.liferay.commerce.constants.CPDefinitionInventoryConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.model.CommerceMoney;
import com.liferay.commerce.currency.util.CommercePriceFormatter;
import com.liferay.commerce.model.CPDefinitionInventory;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.price.CommerceOrderItemPrice;
import com.liferay.commerce.price.CommerceOrderPriceCalculation;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CPInstanceUnitOfMeasure;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPInstanceLocalService;
import com.liferay.commerce.product.service.CPInstanceUnitOfMeasureLocalService;
import com.liferay.commerce.product.util.CPInstanceHelper;
import com.liferay.commerce.service.CPDefinitionInventoryLocalService;
import com.liferay.commerce.service.CommerceOrderItemService;
import com.liferay.commerce.util.CommerceQuantityFormatter;
import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.headless.commerce.core.util.LanguageUtils;
import com.liferay.headless.commerce.delivery.cart.dto.v1_0.CartItem;
import com.liferay.headless.commerce.delivery.cart.dto.v1_0.Price;
import com.liferay.headless.commerce.delivery.cart.dto.v1_0.Settings;
import com.liferay.headless.commerce.delivery.cart.dto.v1_0.SkuUnitOfMeasure;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.BigDecimalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.language.LanguageResources;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.Locale;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 * @author Alessio Antonio Rendina
 */
@Component(
	property = "dto.class.name=com.liferay.headless.commerce.delivery.cart.dto.v1_0.CartItem",
	service = DTOConverter.class
)
public class CartItemDTOConverter
	implements DTOConverter<CommerceOrderItem, CartItem> {

	@Override
	public String getContentType() {
		return CartItem.class.getSimpleName();
	}

	@Override
	public CartItem toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		CartItemDTOConverterContext cartItemDTOConverterContext =
			(CartItemDTOConverterContext)dtoConverterContext;

		CommerceOrderItem commerceOrderItem =
			_commerceOrderItemService.getCommerceOrderItem(
				(Long)cartItemDTOConverterContext.getId());

		Locale locale = cartItemDTOConverterContext.getLocale();

		return new CartItem() {
			{
				setAdaptiveMediaImageHTMLTag(
					() ->
						_cpInstanceHelper.
							getCPInstanceAdaptiveMediaImageHTMLTag(
								cartItemDTOConverterContext.getAccountId(),
								commerceOrderItem.getCompanyId(),
								commerceOrderItem.getCPInstanceId()));
				setCustomFields(
					() -> {
						ExpandoBridge expandoBridge =
							commerceOrderItem.getExpandoBridge();

						return expandoBridge.getAttributes();
					});
				setErrorMessages(
					() -> _getErrorMessages(commerceOrderItem, locale));
				setId(commerceOrderItem::getCommerceOrderItemId);
				setName(
					() -> commerceOrderItem.getName(
						_language.getLanguageId(locale)));
				setOptions(commerceOrderItem::getJson);
				setParentCartItemId(
					commerceOrderItem::getParentCommerceOrderItemId);
				setPrice(() -> _getPrice(commerceOrderItem, locale));
				setProductId(commerceOrderItem::getCProductId);
				setProductURLs(
					() -> LanguageUtils.getLanguageIdMap(
						_cpDefinitionLocalService.getUrlTitleMap(
							commerceOrderItem.getCPDefinitionId())));
				setQuantity(
					() -> _commerceQuantityFormatter.format(
						commerceOrderItem.getCPInstanceId(),
						commerceOrderItem.getQuantity(),
						commerceOrderItem.getUnitOfMeasureKey()));
				setReplacedSku(commerceOrderItem::getReplacedSku);
				setReplacedSkuId(commerceOrderItem::getReplacedCPInstanceId);
				setSettings(
					() -> _getSettings(commerceOrderItem.getCPInstanceId()));
				setSku(commerceOrderItem::getSku);
				setSkuId(commerceOrderItem::getCPInstanceId);
				setSkuUnitOfMeasure(
					() -> {
						String unitOfMeasureKey =
							commerceOrderItem.getUnitOfMeasureKey();

						if (Validator.isNull(unitOfMeasureKey)) {
							return null;
						}

						CPInstanceUnitOfMeasure cpInstanceUnitOfMeasure =
							_cpInstanceUnitOfMeasureLocalService.
								fetchCPInstanceUnitOfMeasure(
									commerceOrderItem.getCPInstanceId(),
									unitOfMeasureKey);

						if (cpInstanceUnitOfMeasure == null) {
							return null;
						}

						return new SkuUnitOfMeasure() {
							{
								setIncrementalOrderQuantity(
									() -> {
										BigDecimal incrementalOrderQuantity =
											cpInstanceUnitOfMeasure.
												getIncrementalOrderQuantity();

										if (incrementalOrderQuantity == null) {
											return null;
										}

										return incrementalOrderQuantity.
											setScale(
												cpInstanceUnitOfMeasure.
													getPrecision(),
												RoundingMode.HALF_UP);
									});
								setKey(() -> unitOfMeasureKey);
								setName(
									() -> cpInstanceUnitOfMeasure.getName(
										locale));
								setPrecision(
									cpInstanceUnitOfMeasure::getPrecision);
								setPrimary(cpInstanceUnitOfMeasure::isPrimary);
								setPriority(
									cpInstanceUnitOfMeasure::getPriority);
								setRate(
									() -> {
										BigDecimal rate =
											cpInstanceUnitOfMeasure.getRate();

										if (rate == null) {
											return null;
										}

										return rate.setScale(
											cpInstanceUnitOfMeasure.
												getPrecision(),
											RoundingMode.HALF_UP);
									});
							}
						};
					});
				setSubscription(commerceOrderItem::isSubscription);
				setThumbnail(
					() -> _cpInstanceHelper.getCPInstanceThumbnailSrc(
						cartItemDTOConverterContext.getAccountId(),
						commerceOrderItem.getCPInstanceId()));
			}
		};
	}

	private CPDefinitionInventory _getCPDefinitionInventory(long cpInstanceId) {
		CPInstance cpInstance = _cpInstanceLocalService.fetchCPInstance(
			cpInstanceId);

		if (cpInstance != null) {
			return _cpDefinitionInventoryLocalService.
				fetchCPDefinitionInventoryByCPDefinitionId(
					cpInstance.getCPDefinitionId());
		}

		return null;
	}

	private String[] _getErrorMessages(
		CommerceOrderItem commerceOrderItem, Locale locale) {

		CPInstance cpInstance = commerceOrderItem.fetchCPInstance();

		if (cpInstance == null) {
			ResourceBundle resourceBundle = LanguageResources.getResourceBundle(
				locale);

			return new String[] {
				_language.get(
					resourceBundle, "the-product-is-no-longer-available")
			};
		}

		return null;
	}

	private Price _getPrice(CommerceOrderItem commerceOrderItem, Locale locale)
		throws Exception {

		CommerceOrder commerceOrder = commerceOrderItem.getCommerceOrder();

		CommerceCurrency commerceCurrency = commerceOrder.getCommerceCurrency();

		CommerceOrderItemPrice commerceOrderItemPrice =
			_commerceOrderPriceCalculation.getCommerceOrderItemPricePerUnit(
				commerceCurrency, commerceOrderItem);

		CommerceMoney unitPriceCommerceMoney =
			commerceOrderItemPrice.getUnitPrice();

		BigDecimal unitPrice = unitPriceCommerceMoney.getPrice();

		CommerceMoney promoPriceCommerceMoney =
			commerceOrderItemPrice.getPromoPrice();

		CommerceMoney discountAmountCommerceMoney =
			commerceOrderItemPrice.getDiscountAmount();

		CommerceMoney finalPriceCommerceMoney =
			commerceOrderItemPrice.getFinalPrice();

		BigDecimal finalPriceCommerceMoneyPrice =
			finalPriceCommerceMoney.getPrice();

		return new Price() {
			{
				setCurrency(() -> commerceCurrency.getName(locale));
				setDiscount(
					() -> {
						if (discountAmountCommerceMoney != null) {
							BigDecimal discountAmount =
								discountAmountCommerceMoney.getPrice();

							if (discountAmount != null) {
								return discountAmount.doubleValue();
							}
						}

						return null;
					});

				setDiscountFormatted(
					() -> {
						if ((discountAmountCommerceMoney != null) &&
							(discountAmountCommerceMoney.getPrice() != null)) {

							return discountAmountCommerceMoney.format(locale);
						}

						return null;
					});

				setDiscountPercentage(
					() -> {
						if ((discountAmountCommerceMoney != null) &&
							(discountAmountCommerceMoney.getPrice() != null)) {

							return _commercePriceFormatter.format(
								commerceOrderItemPrice.getDiscountPercentage(),
								locale);
						}

						return null;
					});

				setDiscountPercentageLevel1(
					() -> {
						if ((discountAmountCommerceMoney != null) &&
							(discountAmountCommerceMoney.getPrice() != null)) {

							BigDecimal discountPercentageLevel1 =
								commerceOrderItemPrice.
									getDiscountPercentageLevel1();

							return discountPercentageLevel1.doubleValue();
						}

						return null;
					});

				setDiscountPercentageLevel2(
					() -> {
						if ((discountAmountCommerceMoney != null) &&
							(discountAmountCommerceMoney.getPrice() != null)) {

							BigDecimal discountPercentageLevel2 =
								commerceOrderItemPrice.
									getDiscountPercentageLevel2();

							return discountPercentageLevel2.doubleValue();
						}

						return null;
					});

				setDiscountPercentageLevel3(
					() -> {
						if ((discountAmountCommerceMoney != null) &&
							(discountAmountCommerceMoney.getPrice() != null)) {

							BigDecimal discountPercentageLevel3 =
								commerceOrderItemPrice.
									getDiscountPercentageLevel3();

							return discountPercentageLevel3.doubleValue();
						}

						return null;
					});

				setDiscountPercentageLevel4(
					() -> {
						if ((discountAmountCommerceMoney != null) &&
							(discountAmountCommerceMoney.getPrice() != null)) {

							BigDecimal discountPercentageLevel4 =
								commerceOrderItemPrice.
									getDiscountPercentageLevel4();

							return discountPercentageLevel4.doubleValue();
						}

						return null;
					});
				setFinalPrice(
					() -> {
						if (finalPriceCommerceMoneyPrice != null) {
							return finalPriceCommerceMoneyPrice.doubleValue();
						}

						return null;
					});
				setFinalPriceFormatted(
					() -> {
						if (finalPriceCommerceMoneyPrice != null) {
							return finalPriceCommerceMoney.format(locale);
						}

						return null;
					});
				setPrice(unitPrice::doubleValue);
				setPriceFormatted(() -> unitPriceCommerceMoney.format(locale));
				setPriceOnApplication(
					commerceOrderItemPrice::isPriceOnApplication);
				setPromoPrice(
					() -> {
						if (promoPriceCommerceMoney != null) {
							BigDecimal unitPromoPrice =
								promoPriceCommerceMoney.getPrice();

							if (unitPromoPrice != null) {
								return unitPromoPrice.doubleValue();
							}
						}

						return null;
					});
				setPromoPriceFormatted(
					() -> {
						if ((promoPriceCommerceMoney != null) &&
							(promoPriceCommerceMoney.getPrice() != null)) {

							return promoPriceCommerceMoney.format(locale);
						}

						return null;
					});
			}
		};
	}

	private Settings _getSettings(long cpInstanceId) {
		CPDefinitionInventory cpDefinitionInventory = _getCPDefinitionInventory(
			cpInstanceId);

		return new Settings() {
			{
				setAllowedQuantities(
					() -> {
						if (cpDefinitionInventory != null) {
							BigDecimal[] allowedOrderQuantitiesArray =
								cpDefinitionInventory.
									getAllowedOrderQuantitiesArray();

							if ((allowedOrderQuantitiesArray != null) &&
								(allowedOrderQuantitiesArray.length > 0)) {

								return allowedOrderQuantitiesArray;
							}
						}

						return null;
					});

				setMaxQuantity(
					() -> {
						BigDecimal maxOrderQuantity =
							CPDefinitionInventoryConstants.
								DEFAULT_MAX_ORDER_QUANTITY;

						if (cpDefinitionInventory != null) {
							maxOrderQuantity =
								cpDefinitionInventory.getMaxOrderQuantity();
						}

						if (maxOrderQuantity != null) {
							return BigDecimalUtil.stripTrailingZeros(
								maxOrderQuantity);
						}

						return null;
					});

				setMinQuantity(
					() -> {
						BigDecimal minOrderQuantity =
							CPDefinitionInventoryConstants.
								DEFAULT_MIN_ORDER_QUANTITY;

						if (cpDefinitionInventory != null) {
							minOrderQuantity =
								cpDefinitionInventory.getMinOrderQuantity();
						}

						if (minOrderQuantity != null) {
							return BigDecimalUtil.stripTrailingZeros(
								minOrderQuantity);
						}

						return null;
					});

				setMultipleQuantity(
					() -> {
						BigDecimal multipleQuantity =
							CPDefinitionInventoryConstants.
								DEFAULT_MULTIPLE_ORDER_QUANTITY;

						if (cpDefinitionInventory != null) {
							multipleQuantity =
								cpDefinitionInventory.
									getMultipleOrderQuantity();
						}

						if (multipleQuantity != null) {
							return BigDecimalUtil.stripTrailingZeros(
								multipleQuantity);
						}

						return null;
					});
			}
		};
	}

	@Reference
	private CommerceOrderItemService _commerceOrderItemService;

	@Reference
	private CommerceOrderPriceCalculation _commerceOrderPriceCalculation;

	@Reference
	private CommercePriceFormatter _commercePriceFormatter;

	@Reference
	private CommerceQuantityFormatter _commerceQuantityFormatter;

	@Reference
	private CPDefinitionInventoryLocalService
		_cpDefinitionInventoryLocalService;

	@Reference
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Reference
	private CPInstanceHelper _cpInstanceHelper;

	@Reference
	private CPInstanceLocalService _cpInstanceLocalService;

	@Reference
	private CPInstanceUnitOfMeasureLocalService
		_cpInstanceUnitOfMeasureLocalService;

	@Reference
	private Language _language;

}