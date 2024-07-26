package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MercadoOrderDmpHandler extends TikTokDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                //销售平台
                Object statusObj = dmpDataMap.get("platformOriginalStatus");
                if (statusObj != null) {
                    String status = String.valueOf(statusObj);
                    if ("ON_HOLD".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_FROZEN.getCode());

                    } else if ("AWAITING_SHIPMENT".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());

                    } else if ("AWAITING_COLLECTION".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());

                    } else if ("PARTIALLY_SHIPPING".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());

                    } else if ("IN_TRANSIT".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());

                    } else if ("DELIVERED".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());

                    } else if ("COMPLETED".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());

                    } else if ("CANCELLED".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                    }
                }

            }
        }
    }
}
