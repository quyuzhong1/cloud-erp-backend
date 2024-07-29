package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.oms.enums.MercadoOrderLogisticTypeEnum;
import com.erp.model.oms.enums.OrderLogisticTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MercadoOrderDmpHandler extends MercadoDmpHandler {




    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        String id = dmpInputTaskEntity.getId();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, id).list();
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        DmpInputTaskEntity dmpInputTaskEntity = list.stream().filter(req -> "1816384798088779541".equals(req.getCfgInputId())).findFirst().orElse(null);

        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getId()));
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "mercadolibre_shipment_data");

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Map<String, Object> shipmentIdMap = (Map<String, Object>) dmpDataMap.get("shipping");
                Object shipmentId = shipmentIdMap.get("fid");
                if (shipmentId != null) {
                    Map<String, Object> shipmentMap = dmpInputMongoChildList.stream().filter(req -> req.get("fid").equals(shipmentId)).findFirst().orElse(null);





                    //物流状态
                    Map<String, Object> logisticMap = (Map<String, Object>) shipmentMap.get("logistic");
                    String logisticType = "";
                    Object modeObj = shipmentMap.get("mode");
                    Object typeObj = shipmentMap.get("type");
                    if (shipmentId != null) {
                        String mode = shipmentMap.get("mode").toString();
                        String type = shipmentMap.get("typeObj").toString();

                        if ("me2".equalsIgnoreCase(mode) && MercadoOrderLogisticTypeEnum.FULFILLMENT.getCode().equalsIgnoreCase(type)) {
                            //如果是平台仓，状态审核通过
                            logisticType = OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode();
                        } else if ("me2".equalsIgnoreCase(mode)
                                && (MercadoOrderLogisticTypeEnum.DROP_OFF.getCode().equals(type) || MercadoOrderLogisticTypeEnum.CROSS_DOCKING.getCode().equalsIgnoreCase(type))
                        ){
                            logisticType = OrderLogisticTypeEnum.TRANSIT_WAREHOUSE.getCode();
                        } else if ("me1".equalsIgnoreCase(mode)) {
                            //自发货
                            logisticType = OrderLogisticTypeEnum.SELF_SHIPMENT.getCode();
                        }
                    }

                    //作废状态
                    Object statusObj = shipmentMap.get("status");
                    if (statusObj != null) {
                        String status = String.valueOf(statusObj);
                        if ("cancelled".equalsIgnoreCase(status)) {
                            dmpDataMap.put("invalidStatus", Boolean.TRUE);
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                        } else if ("shipped".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        } else if ("delivered".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        } else if ("not_delivered".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        } else if ("handling".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                            if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                                dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                                dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
                            } else {
                                dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                            }
                        } else if ("ready_to_ship".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                            if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                                dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                                dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
                            } else {
                                dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                            }
                        }
                    }

                    //订单状态
                    Object orderStatusObj = dmpDataMap.get("status");
                    if (orderStatusObj != null) {
                        String orderStatus = String.valueOf(statusObj);
                        if ("invalid".equalsIgnoreCase(orderStatus)) {
                            dmpDataMap.put("invalidStatus", Boolean.TRUE);
                        } else if ("cancelled".equalsIgnoreCase(orderStatus)) {
                            dmpDataMap.put("invalidStatus", Boolean.TRUE);
                        }
                    }


                }





                //物流信息
//                dmpInputMongoChildList.stream().filter(req -> req.get(""))
                dmpDataMap.get("");

            }
        }
    }
}
