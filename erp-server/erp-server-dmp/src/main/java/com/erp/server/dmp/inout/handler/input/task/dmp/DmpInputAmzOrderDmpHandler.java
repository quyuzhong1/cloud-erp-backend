package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzOrderDmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private DmpSoInfoService dmpSoInfoService;

    @Resource
    private DmpSoDetailService dmpSoDetailService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        Set<List<Map<String, Object>>> keySet = dmpInputDataDmpRelationMaps.keySet();
        if (CollUtil.isEmpty(keySet)) {
            return;
        }
        List<String> orderIdList = new ArrayList<>();
        for (List<Map<String, Object>> key : keySet) {
            orderIdList.addAll(key.stream().map(f -> f.get("amazonOrderId").toString()).collect(Collectors.toList()));
        }

        // 查询明细信息
//        List<ParamData> paramDataList = new ArrayList<>();
//        paramDataList.add(new ParamData("amazonOrderId", "amazonOrderId", PannoEnum.IN, orderIdList));
//        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
//        List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, "amazon_order_items_data");

        // 移除马帮
        List<DmpSoInfoEntity> otherlist = dmpSoInfoService.lambdaQuery()
                .in(DmpSoInfoEntity::getPlatformCode, orderIdList)
                .in(DmpSoInfoEntity::getSourceSystem, DmpBasicSystemCodeEnum.MABANG.getCode())
                .select(DmpSoInfoEntity::getId)
                .list();
        if (!CollectionUtils.isEmpty(otherlist)) {
            List<String> ids = otherlist.stream().map(DmpSoInfoEntity::getId).collect(Collectors.toList());
            dmpSoInfoService.removeByIds(ids);
            dmpSoDetailService.lambdaUpdate()
                    .in(DmpSoDetailEntity::getMainId, ids)
                    .eq(DmpSoDetailEntity::getIsDeleted, false)
                    .set(DmpSoDetailEntity::getIsDeleted, true)
                    .update();
        }
        // Map<订单ID, List<订单明细>>
//        Map<String, List<Map<String, Object>>> orderIdDetailMaps = findMongoData.stream()
//                .collect(Collectors.groupingBy(f -> f.get("amazonOrderId").toString()));

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap :
                dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoDataMap = mongoDataMaps.get(0);
            Order sourceOrder = JSON.parseObject(JSON.toJSONString(mongoDataMap), Order.class);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
//                dmpDataMap.put("shopId", nextLevelId);

                // 付款金额
                BigDecimal payMount = null == sourceOrder.getOrderTotal() ? BigDecimal.ZERO : new BigDecimal(sourceOrder.getOrderTotal().getAmount());
                dmpDataMap.put("payAmount", payMount);

                // 币别（原币）
                String currencyCode = null == sourceOrder.getOrderTotal() ? "" : sourceOrder.getOrderTotal().getCurrencyCode();
                dmpDataMap.put("currencyCode", currencyCode);

                // 平台取消
                boolean isCancel = sourceOrder.convertCancel();
                dmpDataMap.put("isCancel", isCancel);
                // 平台作废
                dmpDataMap.put("invalidStatus", sourceOrder.convertCancel());


                dmpDataMap.put("shippingAmount", BigDecimal.ZERO);

                dmpDataMap.put("buyerRemark", "");

                // 来源类型/多渠道订单/B2C销售订单
                String sourceType = sourceOrder.hasMultiChannel() ? "soMultiChannel" : "soB2c";
                dmpDataMap.put("sourceType", sourceType);

                // 标签json
                Map<String, String> lableMap = new HashMap<>();
                if (Order.FulfillmentChannelEnum.AFN.getValue().equalsIgnoreCase(sourceOrder.getFulfillmentChannel().getValue())) {
                    lableMap.put("fulfillmentChannel", "AFN");
                }
                if (Order.OrderStatusEnum.UNFULFILLABLE.getValue().equalsIgnoreCase(sourceOrder.getOrderStatus())) {
                    lableMap.put("amazonStatus", "Unfulfillable");
                }
                dmpDataMap.put("extendData", JSON.toJSONString(lableMap));

                dmpDataMap.put("platformOrderStatus", sourceOrder.getOrderStatus());

                dmpDataMap.put("orderStatus", sourceOrder.convertBillStatus());
                dmpDataMap.put("payStatus", sourceOrder.convertPayStatus());
                // 审核状态状态
                // （ApproveStatus字典类型）
                dmpDataMap.put("approveStatus", sourceOrder.convertApproveStatusStr());
            }
        }
        log.debug("DmpInputAmzOrderDmpHandler 处理完成: taskId={}", dmpInputTaskEntity.getId());
    }

}
