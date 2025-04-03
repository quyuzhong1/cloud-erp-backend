package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.erp.server.dmp.service.DmpSoReceiverService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class TikTokFullyOrderDmpHandler extends DmpInputDbConvertDmpHandler {
    @Resource
    private DmpSoInfoService dmpSoInfoService;
    @Resource
    private DmpSoReceiverService dmpSoReceiverService;
    @Resource
    private DmpSoDetailService dmpSoDetailService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);

        Set<List<Map<String, Object>>> keySet = dmpInputDataDmpRelationMaps.keySet();
        if (CollUtil.isNotEmpty(keySet)) {
            List<String> orderIdList = new ArrayList<>();
            for (List<Map<String, Object>> key : keySet) {
                orderIdList.addAll(key.stream().map(f -> f.get("code").toString()).collect(Collectors.toList()));
            }

            List<DmpSoInfoEntity> list = dmpSoInfoService.lambdaQuery()
                    .in(DmpSoInfoEntity::getThirdCode, orderIdList)
                    .in(DmpSoInfoEntity::getSourceSystem, Arrays.asList(DmpBasicSystemCodeEnum.KINGDEE.getCode(), DmpBasicSystemCodeEnum.MABANG.getCode()))
                    .select(DmpSoInfoEntity::getId)
                    .list();
            if (CollUtil.isNotEmpty(list)) {
                List<String> ids = list.stream().map(DmpSoInfoEntity::getId).collect(Collectors.toList());
                dmpSoInfoService.removeByIds(ids);
                dmpSoReceiverService.lambdaUpdate()
                        .in(DmpSoReceiverEntity::getMainId, ids)
                        .remove();
                dmpSoDetailService.lambdaUpdate()
                        .in(DmpSoDetailEntity::getMainId, ids)
                        .remove();
            }
        }


        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("nextLevelId", nextLevelId);
                dmpDataMap.put("shopId", nextLevelId);
                dmpDataMap.put("platform_code",dmpDataMap.get("code"));
                dmpDataMap.put("third_code",dmpDataMap.get("code"));

                //销售平台
                Object statusObj = dmpDataMap.get("status");
                if (statusObj != null) {
                    String status = String.valueOf(statusObj);
                    dmpDataMap.put("platformOriginalStatus",status);
                    if ("WAIT_CONFIRM".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    } else if ("WAIT_SEND".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    } else if ("INVAILD".equalsIgnoreCase(status)) {
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                    } else {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    }
                }
                buildExtendData(dmpDataMap);

                //下单时间
                Object createTimeObj = dmpDataMap.get("createTime");
                if (createTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime createTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(createTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformCreateTime", createTime);
                } else {
                    dmpDataMap.put("platformCreateTime", null);
                }
                //更新时间
                Object latestStatusUpdateTimeObj = dmpDataMap.get("latestStatusUpdateTime");
                if (latestStatusUpdateTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime updateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(latestStatusUpdateTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformUpdateTime", updateTime);
                } else {
                    dmpDataMap.put("platformUpdateTime", null);
                }
            }
        }

    }

    private void buildExtendData(TreeMap<String, Object> dmpDataMap) {
        Map<String, String> extendDataMap = new HashMap<>();
        extendDataMap.put("emergencyLevel",dmpDataMap.get("emergencyLevel") + "");
        extendDataMap.put("deliveryType",dmpDataMap.get("type") + "");
        extendDataMap.put("isDeliver",dmpDataMap.get("canDeliver") + "");
        extendDataMap.put("deliveryQty",dmpDataMap.get("deliveredQuantity") + "");
        extendDataMap.put("receiveQty",dmpDataMap.get("receivedQuantity") + "");
        extendDataMap.put("instockQty",dmpDataMap.get("inboundQuantity") + "");
        extendDataMap.put("returnQty",dmpDataMap.get("returnedQuantity") + "");
        extendDataMap.put("orderSourceType",dmpDataMap.get("source") + "");
        Object requireShipTimeObj = dmpDataMap.get("requireShipTime");
        if (requireShipTimeObj != null) {
            // 使用Instant类将Unix时间戳转换为LocalDateTime对象
            LocalDateTime requireShipTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(requireShipTimeObj + "")), ZoneId.systemDefault());
            dmpDataMap.put("requiredDeliveryTime", requireShipTime);
        }
        Object requireArrivedTimeObj = dmpDataMap.get("requireArrivedTime");
        if (requireArrivedTimeObj != null) {
            // 使用Instant类将Unix时间戳转换为LocalDateTime对象
            LocalDateTime requireArrivedTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(requireArrivedTimeObj + "")), ZoneId.systemDefault());
            dmpDataMap.put("requiredReceiveTime", requireArrivedTime);
        }
        dmpDataMap.put("extendData", JSONUtil.toJsonStr(extendDataMap));
    }
}
