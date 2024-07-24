package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
import com.erp.model.dmp.enums.MabangOriginalOrderStatusEnum;
import com.erp.model.dmp.enums.MabangSourcePlatformEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 订单主表字段映射转换
 */
@Slf4j
@Service
@Scope("prototype")
public class ShopifyOrderDmpHandler extends ShopifyDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                //状态
                Object financialStatusObj = dmpDataMap.get("financialStatus");
                log.warn("1Shopify状态转换：{}" , financialStatusObj);
                if (financialStatusObj != null) {
                    String financialStatus = String.valueOf(financialStatusObj);
                    if ("fulfilled".equalsIgnoreCase(financialStatus)) {
                        log.warn("Shopify2状态转换：{}" , financialStatus);
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                    } else if ("refunded".equalsIgnoreCase(financialStatus)) {
                        log.warn("Shopify3状态转换：{}" , financialStatus);
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.REJECT.getCode());
                        dmpDataMap.put("returnStatus", DmpOrderReturnStatusEnum.ORDER_RETURN.getCode());
                    } else if ("partially_refunded".equalsIgnoreCase(financialStatus)) {
                        log.warn("Shopify4状态转换：{}" , financialStatus);
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.REJECT.getCode());
                        dmpDataMap.put("returnStatus", DmpOrderReturnStatusEnum.PARTIAL_RETURN.getCode());
                    } else if ("voided".equalsIgnoreCase(financialStatus)) {
                        log.warn("Shopify5状态转换：{}" , financialStatus);
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.REJECT.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                    } else {
                        log.warn("Shopify6状态转换：{}" , financialStatus);
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                    }
                }
                //买家备注
                Object customerObj = dmpDataMap.get("customer");
                if (customerObj != null) {
                    Map<String,Object> customerMap = (Map<String, Object>) customerObj;
                    dmpDataMap.put("buyerRemark", customerMap.get("note"));
                }
                //税率
                Object taxLinesObj = dmpDataMap.get("taxLines");
                if (taxLinesObj != null) {
                    List<Map<String, Object>> taxLinesList = (List<Map<String, Object>>) taxLinesObj;
                    if (CollectionUtil.isNotEmpty(taxLinesList)) {
                        dmpDataMap.put("taxRate", taxLinesList.get(0).get("rate"));
                    }
                }
                //税率
                Object shippingLinesObj = dmpDataMap.get("shippingLines");
                if (shippingLinesObj != null) {
                    List<Map<String, Object>> shippingLinesList = (List<Map<String, Object>>) shippingLinesObj;
                    if (CollectionUtil.isNotEmpty(shippingLinesList)) {
                        BigDecimal shippingAmount = shippingLinesList.stream().map(req -> MathUtil.valueOf(req.get("price"))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                        dmpDataMap.put("shippingAmount", shippingAmount);
                    }
                }
            }
        }
    }
}
