package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.erp.model.wms.enums.B2BThirdDeliveryCancelResultEnum;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import com.erp.model.wms.enums.ZhongBaoB2BDeliveryStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * B2B三方仓出库状态 RocketMQ 输出处理器
 */
@Service
@Scope("prototype")
public class B2bThirdOutboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<String, DmpThirdOutboundEntity> entityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : dmpRequest.getConvertInputDmpBaseEntityListMaps().entrySet()) {
            if (!"dmp_third_outbound".equals(entry.getKey().getStorageName()) || CollUtil.isEmpty(entry.getValue())) {
                continue;
            }
            for (BaseEntity baseEntity : entry.getValue()) {
                DmpThirdOutboundEntity entity = (DmpThirdOutboundEntity) baseEntity;
                entityMap.put(entity.getId(), entity);
            }
        }

        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : dmpRequest.getChangeConvertInputDmpBaseEntityListMaps().entrySet()) {
            if (!"dmp_third_outbound".equals(entry.getKey().getStorageName()) || CollUtil.isEmpty(entry.getValue())) {
                continue;
            }
            for (BaseEntity baseEntity : entry.getValue()) {
                changeIds.add(baseEntity.getId());
            }
        }

        Map<String, String> result = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpThirdOutboundEntity entity = entityMap.get(changeId);
            PlatformOutboundDTO dto = convert(entity, cfgOutputId);
            if (Objects.nonNull(dto)) {
                result.put(entity.getId(), JSON.toJSONString(dto));
            }
        }
        return result;
    }

    public PlatformOutboundDTO convert(DmpThirdOutboundEntity entity, String cfgOutputId) {
        if (Objects.isNull(entity) || this.validateDataBlack(entity, cfgOutputId)) {
            return null;
        }
        String thirdOrderStatus = entity.getOrderStatus();
        String targetStatus = convertStatus(entity.getSourcePlatform(), thirdOrderStatus);
        PlatformOutboundDTO dto = BeanUtil.copyProperties(entity, PlatformOutboundDTO.class);
        dto.setUniqueId(entity.getReferenceNo());
        dto.setPlatform(entity.getSourcePlatform());
        dto.setProvider(entity.getSourcePlatform());
        dto.setTrackNo(entity.getTrackingNo());
        dto.setOutBoundTime(entity.getDateShipping());
        dto.setThirdOrderStatus(thirdOrderStatus);
        dto.setOrderStatus(targetStatus);
        return dto;
    }

    private String convertStatus(String sourcePlatform, String thirdOrderStatus) {
        if (PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode().equalsIgnoreCase(sourcePlatform)) {
            ZhongBaoB2BDeliveryStatusEnum statusEnum = ZhongBaoB2BDeliveryStatusEnum.getByCode(thirdOrderStatus);
            return Objects.nonNull(statusEnum) ? statusEnum.getErpStatus().getCode() : null;
        }
        B2BThirdDeliveryCancelResultEnum statusEnum = B2BThirdDeliveryCancelResultEnum.getByCode(thirdOrderStatus);
        if (Objects.isNull(statusEnum)) {
            return null;
        }
        switch (statusEnum) {
            case NEW:
                return ThirdDeliveryStatusEnum.CREATING.getCode();
            case SUBMIT:
            case PROCESSED:
            case WAIT_UPLOAD:
            case UPLOADED:
                return ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode();
            case SUCCESS:
                return ThirdDeliveryStatusEnum.SHIPPED.getCode();
            case DISCARD:
            case PROBLEM:
                return ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode();
            case BLOCK:
            case DISCARD_PROCESSED:
                return ThirdDeliveryStatusEnum.INTERCEPTING.getCode();
            case EXCEPTION:
                return ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode();
            default:
                return null;
        }
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("orderCode");
    }
}
