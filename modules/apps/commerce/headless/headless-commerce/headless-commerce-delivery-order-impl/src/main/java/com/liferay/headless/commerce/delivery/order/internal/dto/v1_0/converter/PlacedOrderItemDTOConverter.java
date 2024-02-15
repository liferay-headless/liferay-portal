/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.order.internal.dto.v1_0.converter;

import com.liferay.commerce.constants.CPDefinitionInventoryConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.model.CommerceMoney;
import com.liferay.commerce.currency.util.CommercePriceFormatter;
import com.liferay.commerce.media.CommerceMediaResolver;
import com.liferay.commerce.model.CPDefinitionInventory;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.price.CommerceOrderItemPrice;
import com.liferay.commerce.price.CommerceOrderPriceCalculation;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPInstanceLocalService;
import com.liferay.commerce.product.type.virtual.order.model.CommerceVirtualOrderItem;
import com.liferay.commerce.product.type.virtual.order.model.CommerceVirtualOrderItemFileEntry;
import com.liferay.commerce.product.type.virtual.order.service.CommerceVirtualOrderItemService;
import com.liferay.commerce.product.util.CPInstanceHelper;
import com.liferay.commerce.service.CPDefinitionInventoryLocalService;
import com.liferay.commerce.service.CommerceOrderItemService;
import com.liferay.commerce.util.CommerceQuantityFormatter;
import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.headless.commerce.core.util.LanguageUtils;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.PlacedOrderItem;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Price;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Settings;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.VirtualItem;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.BigDecimalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.language.LanguageResources;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import java.math.BigDecimal;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	property = "dto.class.name=com.liferay.headless.commerce.delivery.order.dto.v1_0.PlacedOrderItem",
	service = DTOConverter.class
)
public class PlacedOrderItemDTOConverter
	implements DTOConverter<CommerceOrderItem, PlacedOrderItem> {

	@Override
	public String getContentType() {
		return PlacedOrderItem.class.getSimpleName();
	}

	@Override
	public PlacedOrderItem toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		PlacedOrderItemDTOConverterContext placedOrderItemDTOConverterContext =
			(PlacedOrderItemDTOConverterContext)dtoConverterContext;

		CommerceOrderItem commerceOrderItem =
			_commerceOrderItemService.getCommerceOrderItem(
				(Long)placedOrderItemDTOConverterContext.getId());

		Locale locale = placedOrderItemDTOConverterContext.getLocale();

		return new PlacedOrderItem() {
			{
				setAdaptiveMediaImageHTMLTag(
					() ->
						_cpInstanceHelper.
							getCPInstanceAdaptiveMediaImageHTMLTag(
								placedOrderItemDTOConverterContext.
									getAccountId(),
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
				setParentOrderItemId(
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
				setSettings(
					() -> _getSettings(commerceOrderItem.getCPInstanceId()));
				setSku(commerceOrderItem::getSku);
				setSkuId(commerceOrderItem::getCPInstanceId);
				setSubscription(commerceOrderItem::isSubscription);
				setThumbnail(
					() -> _cpInstanceHelper.getCPInstanceThumbnailSrc(
						placedOrderItemDTOConverterContext.getAccountId(),
						commerceOrderItem.getCPInstanceId()));
				setUnitOfMeasureKey(commerceOrderItem::getUnitOfMeasureKey);

				setVirtualItems(
					() -> {
						try {
							CommerceVirtualOrderItem commerceVirtualOrderItem =
								_commerceVirtualOrderItemService.
									fetchCommerceVirtualOrderItemByCommerceOrderItemId(
										commerceOrderItem.
											getCommerceOrderItemId());

							if (commerceVirtualOrderItem == null) {
								return null;
							}

							return _toVirtualItems(
								commerceVirtualOrderItem.
									getCommerceVirtualOrderItemFileEntries(),
								commerceVirtualOrderItem);
						}
						catch (PortalException portalException) {
							if (_log.isDebugEnabled()) {
								_log.debug(portalException);
							}

							return null;
						}
					});
				setVirtualItemURLs(
					() -> {
						try {
							CommerceVirtualOrderItem commerceVirtualOrderItem =
								_commerceVirtualOrderItemService.
									fetchCommerceVirtualOrderItemByCommerceOrderItemId(
										commerceOrderItem.
											getCommerceOrderItemId());

							if (commerceVirtualOrderItem == null) {
								return null;
							}

							List<CommerceVirtualOrderItemFileEntry>
								commerceVirtualOrderItemFileEntries =
									commerceVirtualOrderItem.
										getCommerceVirtualOrderItemFileEntries();

							if (commerceVirtualOrderItemFileEntries.isEmpty()) {
								return null;
							}

							CommerceVirtualOrderItemFileEntry
								commerceVirtualOrderItemFileEntry =
									commerceVirtualOrderItemFileEntries.get(0);

							String url =
								commerceVirtualOrderItemFileEntry.getUrl();

							if (Validator.isBlank(url)) {
								url =
									_commerceMediaResolver.
										getDownloadVirtualOrderItemURL(
											commerceVirtualOrderItem.
												getCommerceVirtualOrderItemId(),
											commerceVirtualOrderItemFileEntry.
												getFileEntryId());
							}

							return new String[] {url};
						}
						catch (PortalException portalException) {
							if (_log.isDebugEnabled()) {
								_log.debug(portalException);
							}

							return null;
						}
					});
			}
		};
	}

	private CPDefinitionInventory _getCpDefinitionInventory(long cpInstanceId) {
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

		BigDecimal priceCommerceMoney = finalPriceCommerceMoney.getPrice();

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
						if (priceCommerceMoney != null) {
							return priceCommerceMoney.doubleValue();
						}

						return null;
					});
				setFinalPriceFormatted(
					() -> {
						if (priceCommerceMoney != null) {
							return finalPriceCommerceMoney.format(locale);
						}

						return null;
					});
				setPrice(unitPrice::doubleValue);
				setPriceFormatted(() -> unitPriceCommerceMoney.format(locale));
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
		CPDefinitionInventory cpDefinitionInventory = _getCpDefinitionInventory(
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

	private VirtualItem[] _toVirtualItems(
		List<CommerceVirtualOrderItemFileEntry>
			commerceVirtualOrderItemFileEntries,
		CommerceVirtualOrderItem commerceVirtualOrderItem) {

		return TransformUtil.transformToArray(
			commerceVirtualOrderItemFileEntries,
			commerceVirtualOrderItemFileEntry -> new VirtualItem() {
				{
					setUrl(
						() -> {
							if (Validator.isNull(
									commerceVirtualOrderItemFileEntry.
										getUrl())) {

								return _commerceMediaResolver.
									getDownloadVirtualOrderItemURL(
										commerceVirtualOrderItem.
											getCommerceVirtualOrderItemId(),
										commerceVirtualOrderItemFileEntry.
											getFileEntryId());
							}

							return commerceVirtualOrderItemFileEntry.getUrl();
						});
					setUsages(commerceVirtualOrderItemFileEntry::getUsages);
					setVersion(
						() -> {
							if (Validator.isNull(
									commerceVirtualOrderItemFileEntry.
										getVersion())) {

								return null;
							}

							return commerceVirtualOrderItemFileEntry.
								getVersion();
						});
				}
			},
			VirtualItem.class);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PlacedOrderItemDTOConverter.class);

	@Reference
	private CommerceMediaResolver _commerceMediaResolver;

	@Reference
	private CommerceOrderItemService _commerceOrderItemService;

	@Reference
	private CommerceOrderPriceCalculation _commerceOrderPriceCalculation;

	@Reference
	private CommercePriceFormatter _commercePriceFormatter;

	@Reference
	private CommerceQuantityFormatter _commerceQuantityFormatter;

	@Reference
	private CommerceVirtualOrderItemService _commerceVirtualOrderItemService;

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
	private Language _language;

}