package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ObjectUtils;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
import com.erp.model.dmp.enums.MabangOriginalOrderStatusEnum;
import com.erp.model.dmp.enums.MabangSourcePlatformEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.oms.shopify.api.rest.model.ShopifyOrder;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefund;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefundLineItem;
import com.sdk.oms.shopify.api.rest.model.enums.ShopifyOrderFinancialStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单主表字段映射转换
 */
@Slf4j
@Service
@Scope("prototype")
public class ShopifyOrderDmpHandler extends ShopifyDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        String id = dmpInputTaskEntity.getId();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, id).list();
        if (CollectionUtil.isEmpty(list)) {
            return;
        }

        //订单交易
        DmpInputTaskEntity dmpInputTransactionsEntity = list.stream().filter(req -> "1823265118759180922".equals(req.getCfgInputId())).findFirst().orElse(null);

        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTransactionsEntity.getId()));
        List<Map<String, Object>> dmpInputTransactionsMongoChildList = mongoService.findMongoData(paramDataList, "shopify_transactions_data");

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Map<String, Object> lableMap = new HashMap<>();
                //状态
                Object financialStatusObj = dmpDataMap.get("platformOriginalStatus");
                if (financialStatusObj != null) {
                    dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    String financialStatus = String.valueOf(financialStatusObj);

                    ShopifyOrderFinancialStatusEnum statusEnum = ShopifyOrderFinancialStatusEnum.getByCode(financialStatus);
                    if (null == statusEnum) {
                        //待付款
                        dmpDataMap.put("payStatus", Boolean.FALSE);
                    }
                    // 已付款
                    if (ShopifyOrderFinancialStatusEnum.PAID.equals(statusEnum) ||
                            ShopifyOrderFinancialStatusEnum.VOIDED.equals(statusEnum) ||
                            ShopifyOrderFinancialStatusEnum.REFUNDED.equals(statusEnum) ||
                            ShopifyOrderFinancialStatusEnum.PARTIALLY_REFUNDED.equals(statusEnum)
                    ) {
                        dmpDataMap.put("payStatus", Boolean.TRUE);
                    }
                    // 待付款
                    if (ShopifyOrderFinancialStatusEnum.PENDING.equals(statusEnum) ||
                            ShopifyOrderFinancialStatusEnum.UNPAID.equals(statusEnum) ||
                            ShopifyOrderFinancialStatusEnum.AUTHORIZED.equals(statusEnum) ||
                            ShopifyOrderFinancialStatusEnum.PARTIALLY_PAID.equals(statusEnum)
                    ) {
                        dmpDataMap.put("payStatus", Boolean.FALSE);
                    }

                    if ("fulfilled".equalsIgnoreCase(financialStatus)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                    } else if ("refunded".equalsIgnoreCase(financialStatus)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.REJECT.getCode());
                        dmpDataMap.put("returnStatus", DmpOrderReturnStatusEnum.ORDER_RETURN.getCode());
                    } else if ("partially_refunded".equalsIgnoreCase(financialStatus)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.REJECT.getCode());
                        dmpDataMap.put("returnStatus", DmpOrderReturnStatusEnum.PARTIAL_RETURN.getCode());
                    } else if ("voided".equalsIgnoreCase(financialStatus)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.REJECT.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                    } else {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                    }
                }
                //状态
                Object fulfillmentStatusObj = dmpDataMap.get("fulfillmentStatus");
                dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                if (fulfillmentStatusObj != null) {
                    String fulfillmentStatus = String.valueOf(fulfillmentStatusObj);
                    dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                    if (null == fulfillmentStatus || StringUtils.isBlank(fulfillmentStatus)) {
                        // 配货中
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                    }
                    // 已发货
                    if ("fulfilled".equalsIgnoreCase(fulfillmentStatus)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                    } else if ("refunded".equalsIgnoreCase(fulfillmentStatus)) {
                        dmpDataMap.put("returnStatus", DmpOrderReturnStatusEnum.ORDER_RETURN.getCode());
                    }
                }

                //买家备注
                Object customerObj = dmpDataMap.get("customer");
                if (customerObj != null) {
                    Map<String, Object> customerMap = (Map<String, Object>) customerObj;
                    dmpDataMap.put("buyerRemark", customerMap.get("note"));
                }


                //交易信息
                Map<String, Object> transactionsMap = dmpInputTransactionsMongoChildList.stream()
                        .filter(req -> String.valueOf(req.get("orderId")).equals(String.valueOf(dmpDataMap.get("thirdCode"))))
                        .findFirst().orElse(null);
                boolean payStatus = Boolean.parseBoolean(dmpDataMap.getOrDefault("payStatus", false).toString());
                if (ObjectUtil.isNotEmpty(transactionsMap) && payStatus) {
                    dmpDataMap.put("payTime", transactionsMap.get("processedAt"));
                    dmpDataMap.put("payMethod", transactionsMap.get("gateway"));
                }



                //税率
                Object taxLinesObj = dmpDataMap.get("taxLines");
                if (taxLinesObj != null) {
                    List<Map<String, Object>> taxLinesList = (List<Map<String, Object>>) taxLinesObj;
                    if (CollectionUtil.isNotEmpty(taxLinesList)) {
                        dmpDataMap.put("taxRate", taxLinesList.get(0).get("rate"));
                    }
                }
                //运费
                Object shippingLinesObj = dmpDataMap.get("shippingLines");
                if (shippingLinesObj != null) {
                    List<Map<String, Object>> shippingLinesList = (List<Map<String, Object>>) shippingLinesObj;
                    if (CollectionUtil.isNotEmpty(shippingLinesList)) {
                        BigDecimal shippingAmount = shippingLinesList.stream().map(req -> MathUtil.valueOf(req.get("price"))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                        dmpDataMap.put("shippingAmount", shippingAmount);
                    }
                }

                //退款
                Object refundsObj = dmpDataMap.get("refunds");
                if (ObjectUtil.isNotEmpty(refundsObj)) {
                    // 存在退款的明细ID
                    Set<String> refundedLineItemIds = new HashSet<>();

                    List<Object> refundsMap = (List<Object>) refundsObj;
                    if (CollectionUtil.isNotEmpty(refundsMap)) {

                        for (Object map : refundsMap) {
                            Map<String, Object> stringObjectMap = (Map<String, Object>) map;
                            Object refundLineItems = stringObjectMap.get("refundLineItems");
                            if (ObjectUtil.isNotEmpty(refundLineItems)) {
                                List<Map<String, Object>> refundLineItemsList = (List<Map<String, Object>>) refundLineItems;
                                List<String> sourceFundedLineItemIds = refundLineItemsList.stream()
                                        .map(req -> req.get("lineItemId").toString())
                                        .distinct()
                                        .collect(Collectors.toList());
                                refundedLineItemIds.addAll(sourceFundedLineItemIds);

                            }
                        }
                    }
                    lableMap.put("refundedLineItemIds", refundedLineItemIds);
                }

                //卖家订单号
                Object name = dmpDataMap.get("name");
                lableMap.put("sellerOrderCode", name);

                dmpDataMap.put("extendData", JSONUtil.toJsonStr(lableMap));
            }
        }
    }
}
