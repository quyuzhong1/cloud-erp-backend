package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Magalu订单详情转换为DMP订单主表、明细、收件人。
 */
@Service
@Scope("prototype")
public class MagaluOrderDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final String STORAGE_SO_INFO = "dmp_so_info";
    private static final String STORAGE_SO_DETAIL = "dmp_so_detail";
    private static final String STORAGE_SO_RECEIVER = "dmp_so_receiver";
    private static final String MAGALU_PLATFORM = "Magalu";

    @Override
    public List<Map<String, Object>> convertMongoToDmp(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {
        List<Map<String, Object>> orderList = super.convertMongoToDmp(dmpRequest, dmpResponse);
        if (CollUtil.isEmpty(orderList)) {
            return Collections.emptyList();
        }
        if (STORAGE_SO_INFO.equals(storageName)) {
            return buildSoInfoRows(orderList);
        }
        Map<String, String> mainIdMap = buildMainIdMap(dmpResponse);
        if (STORAGE_SO_DETAIL.equals(storageName)) {
            return buildSoDetailRows(orderList, mainIdMap);
        }
        if (STORAGE_SO_RECEIVER.equals(storageName)) {
            return buildSoReceiverRows(orderList, mainIdMap);
        }
        return orderList;
    }

    private List<Map<String, Object>> buildSoInfoRows(List<Map<String, Object>> orderList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> order : orderList) {
            List<Map<String, Object>> deliveryList = listMap(order.get("deliveries"));
            for (Map<String, Object> delivery : deliveryList) {
                Map<String, Object> row = baseRow(order);
                String orderCode = stringValue(order.get("code"));
                String deliveryId = stringValue(delivery.get("id"));
                String thirdCode = buildDeliveryUniqueCode(orderCode, deliveryId);
                Map<String, Object> amounts = mapValue(order.get("amounts"));
                Map<String, Object> deliveryAmounts = mapValue(delivery.get("amounts"));
                Map<String, Object> shipping = mapValue(delivery.get("shipping"));
                Map<String, Object> provider = mapValue(shipping.get("provider"));
                Map<String, Object> payment = firstMap(order.get("payments"));

                row.put("platformCreateTime", parseTime(order.get("created_at")));
                row.put("platformUpdateTime", parseTime(order.get("updated_at")));
                row.put("sourcePlatform", MAGALU_PLATFORM);
                row.put("sourceSystem", MAGALU_PLATFORM);
                row.put("thirdCode", thirdCode);
                row.put("platformCode", orderCode);
                row.put("invalidStatus", isCancel(order.get("status")));
                row.put("isCancel", isCancel(order.get("status")));
                row.put("orderStatus", convertOrderStatus(order.get("status")));
                row.put("deliveryStatus", convertDeliveryStatus(delivery.get("status")));
                row.put("returnStatus", "");
                row.put("platformOriginalStatus", stringValue(order.get("status")));
                row.put("shopId", row.get("nextLevelId"));
                row.put("shopName", "");
                row.put("payTime", parseTime(order.get("purchased_at")));
                row.put("payStatus", CollUtil.isNotEmpty(listMap(order.get("payments"))));
                row.put("payMethod", stringValue(payment.get("method")));
                row.put("currencyCode", firstNotBlank(stringValue(amounts.get("currency")), stringValue(deliveryAmounts.get("currency"))));
                row.put("exchangeRate", BigDecimal.ONE);
                row.put("payAmount", money(amounts.get("total"), amounts.get("normalizer")));
                row.put("allAmount", money(amounts.get("total"), amounts.get("normalizer")));
                row.put("shippingAmount", money(mapValue(amounts.get("freight")).get("total"), amounts.get("normalizer")));
                row.put("platformCost", money(mapValue(amounts.get("commission")).get("total"), amounts.get("normalizer")));
                row.put("totalTaxFee", money(mapValue(amounts.get("tax")).get("total"), amounts.get("normalizer")));
                row.put("deliveryTime", null);
                row.put("logisticsCode", stringValue(delivery.get("code")));
                row.put("logisticsName", firstNotBlank(stringValue(provider.get("description")), stringValue(provider.get("name"))));
                row.put("approveStatus", "");
                row.put("logisticType", "");
                row.put("buyerSelectedLogistics", firstNotBlank(stringValue(provider.get("description")), stringValue(provider.get("name"))));
                row.put("planPackageNo", deliveryId);
                row.put("planSupplierId", stringValue(provider.get("id")));
                row.put("extendData", JSONUtil.toJsonStr(buildExtend(order, delivery)));
                resultList.add(row);
            }
        }
        return resultList;
    }

    private List<Map<String, Object>> buildSoDetailRows(List<Map<String, Object>> orderList, Map<String, String> mainIdMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> order : orderList) {
            String orderCode = stringValue(order.get("code"));
            List<Map<String, Object>> deliveryList = listMap(order.get("deliveries"));
            for (Map<String, Object> delivery : deliveryList) {
                String deliveryId = stringValue(delivery.get("id"));
                String mainId = mainIdMap.get(buildDeliveryUniqueCode(orderCode, deliveryId));
                if (StringUtils.isBlank(mainId)) {
                    continue;
                }
                List<Map<String, Object>> itemList = listMap(delivery.get("items"));
                for (Map<String, Object> item : itemList) {
                    Map<String, Object> info = mapValue(item.get("info"));
                    Map<String, Object> unitPrice = mapValue(item.get("unit_price"));
                    Map<String, Object> amounts = mapValue(item.get("amounts"));
                    Map<String, Object> row = baseRow(order);
                    String lineNo = stringValue(item.get("sequencial"));
                    String sku = stringValue(info.get("sku"));
                    row.put("mainId", mainId);
                    row.put("thirdDetailId", buildDetailId(deliveryId, lineNo, sku));
                    row.put("platformDetailId", buildDetailId(deliveryId, lineNo, sku));
                    row.put("skuId", "");
                    row.put("skuNo", sku);
                    row.put("platformSku", sku);
                    row.put("platformSkuId", stringValue(info.get("id")));
                    row.put("platformSpuNo", stringValue(info.get("id")));
                    row.put("specifics", "");
                    row.put("qty", intValue(item.get("quantity")));
                    row.put("productUnit", stringValue(item.get("measure_unit")));
                    row.put("skuName", firstNotBlank(stringValue(info.get("name")), stringValue(info.get("description"))));
                    row.put("apiGoodsName", firstNotBlank(stringValue(info.get("name")), stringValue(info.get("description"))));
                    row.put("skuUrl", firstImageUrl(info));
                    row.put("exchangeRate", BigDecimal.ONE);
                    row.put("sellPriceOrigin", money(unitPrice.get("value"), unitPrice.get("normalizer")));
                    row.put("sellPrice", money(unitPrice.get("value"), unitPrice.get("normalizer")));
                    row.put("afterAmount", money(amounts.get("total"), amounts.get("normalizer")));
                    row.put("discountAmount", money(mapValue(amounts.get("discount")).get("total"), amounts.get("normalizer")));
                    row.put("shippingCost", money(mapValue(amounts.get("freight")).get("total"), amounts.get("normalizer")));
                    row.put("currencyCode", firstNotBlank(stringValue(amounts.get("currency")), stringValue(unitPrice.get("currency"))));
                    row.put("platformPackageId", deliveryId);
                    row.put("platformStatus", stringValue(delivery.get("status")));
                    row.put("extendData", JSONUtil.toJsonStr(item));
                    resultList.add(row);
                }
            }
        }
        return resultList;
    }

    private List<Map<String, Object>> buildSoReceiverRows(List<Map<String, Object>> orderList, Map<String, String> mainIdMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> order : orderList) {
            Map<String, Object> customer = mapValue(order.get("customer"));
            String orderCode = stringValue(order.get("code"));
            for (Map<String, Object> delivery : listMap(order.get("deliveries"))) {
                String deliveryId = stringValue(delivery.get("id"));
                String mainId = mainIdMap.get(buildDeliveryUniqueCode(orderCode, deliveryId));
                if (StringUtils.isBlank(mainId)) {
                    continue;
                }
                Map<String, Object> shipping = mapValue(delivery.get("shipping"));
                Map<String, Object> recipient = mapValue(shipping.get("recipient"));
                Map<String, Object> address = mapValue(recipient.get("address"));
                Map<String, Object> row = baseRow(order);
                row.put("mainId", mainId);
                row.put("country", stringValue(address.get("country")));
                row.put("buyerId", stringValue(customer.get("document_number")));
                row.put("buyerName", stringValue(customer.get("name")));
                row.put("receiverName", firstNotBlank(stringValue(recipient.get("name")), stringValue(customer.get("name"))));
                row.put("receiverTelNumber", firstNotBlank(stringValue(recipient.get("phone")), stringValue(customer.get("phone"))));
                row.put("postCode", stringValue(address.get("zipcode")));
                row.put("province", stringValue(address.get("state")));
                row.put("city", stringValue(address.get("city")));
                row.put("district", stringValue(address.get("district")));
                row.put("mainStreet", concatAddress(address));
                row.put("secondStreet", stringValue(address.get("complement")));
                row.put("fullAddress", fullAddress(address));
                row.put("mainPhone", firstNotBlank(stringValue(recipient.get("phone")), stringValue(customer.get("phone"))));
                row.put("receiverTaxNo", firstNotBlank(stringValue(recipient.get("document_number")), stringValue(customer.get("document_number"))));
                row.put("email", stringValue(customer.get("email")));
                row.put("taxidType", firstNotBlank(stringValue(recipient.get("customer_type")), stringValue(customer.get("customer_type"))));
                resultList.add(row);
            }
        }
        return resultList;
    }

    private Map<String, String> buildMainIdMap(DmpInputMongoResponse dmpResponse) {
        if (!(dmpResponse instanceof DmpInputDmpResponse)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        DmpInputDmpResponse response = (DmpInputDmpResponse) dmpResponse;
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : response.getConvertInputDmpBaseEntityListMaps().entrySet()) {
            if (!STORAGE_SO_INFO.equals(entry.getKey().getStorageName())) {
                continue;
            }
            for (BaseEntity entity : entry.getValue()) {
                DmpSoInfoEntity soInfo = (DmpSoInfoEntity) entity;
                result.put(soInfo.getThirdCode(), soInfo.getId());
            }
        }
        return result;
    }

    private Map<String, Object> baseRow(Map<String, Object> source) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (String key : DmpInputMongoHandler.mongoBaseFiledList) {
            if (source.containsKey(key)) {
                row.put(key, source.get(key));
            }
        }
        Object nextLevelId = source.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID);
        if (nextLevelId != null) {
            row.put("nextLevelId", nextLevelId.toString());
        }
        return row;
    }

    private String buildDeliveryUniqueCode(String orderCode, String deliveryId) {
        if (StringUtils.isBlank(deliveryId)) {
            return orderCode;
        }
        return orderCode + "_" + deliveryId;
    }

    private String buildDetailId(String deliveryId, String lineNo, String sku) {
        return firstNotBlank(deliveryId, "") + "_" + firstNotBlank(lineNo, "") + "_" + firstNotBlank(sku, "");
    }

    private boolean isCancel(Object statusObj) {
        String status = stringValue(statusObj);
        return "cancelled".equalsIgnoreCase(status) || "canceled".equalsIgnoreCase(status);
    }

    private String convertOrderStatus(Object statusObj) {
        return isCancel(statusObj) ? ApproveStatusEnum.WAIT_SUBMIT.getCode() : ApproveStatusEnum.WAIT_SUBMIT.getCode();
    }

    private String convertDeliveryStatus(Object statusObj) {
        String status = stringValue(statusObj);
        if ("shipped".equalsIgnoreCase(status) || "delivered".equalsIgnoreCase(status)) {
            return SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        }
        return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
    }

    private LocalDateTime parseTime(Object value) {
        String text = stringValue(value);
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal money(Object value, Object normalizer) {
        BigDecimal amount = decimalValue(value);
        BigDecimal divisor = decimalValue(normalizer);
        if (BigDecimal.ZERO.compareTo(divisor) == 0) {
            divisor = BigDecimal.ONE;
        }
        return amount.divide(divisor, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private int intValue(Object value) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return 0;
        }
        try {
            return new BigDecimal(value.toString()).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String firstImageUrl(Map<String, Object> info) {
        List<Map<String, Object>> imageList = listMap(info.get("images"));
        if (CollUtil.isEmpty(imageList)) {
            return "";
        }
        return stringValue(imageList.get(0).get("url"));
    }

    private Map<String, Object> buildExtend(Map<String, Object> order, Map<String, Object> delivery) {
        Map<String, Object> extend = new LinkedHashMap<>();
        extend.put("magaluOrderId", order.get("id"));
        extend.put("magaluOrderCode", order.get("code"));
        extend.put("magaluDeliveryId", delivery.get("id"));
        extend.put("magaluDeliveryCode", delivery.get("code"));
        extend.put("sourceChannel", order.get("source_channel"));
        extend.put("deliveryShipping", delivery.get("shipping"));
        return extend;
    }

    private String concatAddress(Map<String, Object> address) {
        String street = stringValue(address.get("street"));
        String number = stringValue(address.get("number"));
        return firstNotBlank(street + (StringUtils.isBlank(number) ? "" : " " + number), "").trim();
    }

    private String fullAddress(Map<String, Object> address) {
        List<String> parts = new ArrayList<>();
        addIfNotBlank(parts, concatAddress(address));
        addIfNotBlank(parts, stringValue(address.get("complement")));
        addIfNotBlank(parts, stringValue(address.get("district")));
        addIfNotBlank(parts, stringValue(address.get("city")));
        addIfNotBlank(parts, stringValue(address.get("state")));
        addIfNotBlank(parts, stringValue(address.get("country")));
        addIfNotBlank(parts, stringValue(address.get("zipcode")));
        return StringUtils.join(parts, ", ");
    }

    private void addIfNotBlank(List<String> list, String value) {
        if (StringUtils.isNotBlank(value)) {
            list.add(value);
        }
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> firstMap(Object value) {
        List<Map<String, Object>> list = listMap(value);
        return CollUtil.isEmpty(list) ? Collections.emptyMap() : list.get(0);
    }

    private List<Map<String, Object>> listMap(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            if (item instanceof Map) {
                result.add((Map<String, Object>) item);
            }
        }
        return result;
    }
}
