package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFbaShipmentReceiveEntity;
import com.erp.model.dmp.lingxing.FbaReceiveDetailEntity;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 领星FBA货件签收推送Handler
 */
@Service
@Scope("prototype")
public class DmpOutputLxFbaReceivedRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    /**
     * 分组推送MQ
     * Map<分组Key, MQ消息体>
     * 分组key={货件ID}_{本地签收日期}_{店铺ID}
     * MQ消息体=FbaReceiveGroupEntity
     *
     */
    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        // 来源信息
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpFbaShipmentReceiveEntity> dmpEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
            if ("dmp_fba_shipment_receive".equals(storageName)) {
                for (BaseEntity v : value) {
                    DmpFbaShipmentReceiveEntity dmpEntity = (DmpFbaShipmentReceiveEntity) v;
                    dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
                }
            }
        }

        // 对比变动
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeFbaShipmentIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
            if ("dmp_fba_shipment_receive".equals(storageName)) {
                for (BaseEntity v : value) {
                    DmpFbaShipmentReceiveEntity dmpEntity = (DmpFbaShipmentReceiveEntity) v;
                    changeFbaShipmentIds.add(dmpEntity.getFbaShipmentId());
                }
            }
        }
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();

        Map<String, List<FbaReceiveDetailEntity>> sourceGroupMap = dmpEntityMap.values().stream()
                .filter(ObjectUtil::isNotEmpty)
                .map(e -> this.convert(e, cfgOutputId))
                .filter(e -> changeFbaShipmentIds.contains(e.getFbaShipmentId()))
                .collect(Collectors.groupingBy(FbaReceiveDetailEntity::getFbaShipmentId));

        // 结果类型
        Map<String, String> resultMap = new HashMap<>();
        for (Map.Entry<String, List<FbaReceiveDetailEntity>> entry : sourceGroupMap.entrySet()) {
            FbaReceiveDetailEntity curEntity = entry.getValue().stream().findFirst().orElse(null);
            if (null == curEntity){
                ServiceException.runError("数据异常:FBA签收明细为空");
            }
            String key = StrUtil.format("{}_{}_{}", entry.getKey(), curEntity.getReceivedDateReport().toString(), curEntity.getShopId());
            FbaReceiveGroupEntity groupDto = FbaReceiveGroupEntity.init(entry, curEntity.getReceivedDateReport(), curEntity.getSid().toString(), curEntity.getShopId());
            resultMap.put(key, JSON.toJSONString(groupDto));
        }
        return resultMap;
    }

    /**
     * DMP数据转换推送DTO
     **/
    public FbaReceiveDetailEntity convert(DmpFbaShipmentReceiveEntity dmpEntity, String cfgOutputId) {
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        FbaReceiveDetailEntity dtoEntity = new FbaReceiveDetailEntity();
        BeanUtils.copyProperties(dmpEntity, dtoEntity);
        dtoEntity.setSku(dmpEntity.getMsku());
        dtoEntity.setFnsku(dmpEntity.getFnSku());
        dtoEntity.setFulfillmentCenterId(dmpEntity.getFulfillmentCenter());
        dtoEntity.setReceivedDateLocaleStr(dmpEntity.getReceiveLocaleDate());
        dtoEntity.setReceivedDateStr(dmpEntity.getReceiveDate());
        dtoEntity.setQuantity(dmpEntity.getReceiveQty());
        dtoEntity.setReceivedDateReport(dmpEntity.getReceivedDateReport());
        dtoEntity.setSid(Integer.parseInt(dmpEntity.getThirdId()));
        return dtoEntity;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueMd5");
    }
}
