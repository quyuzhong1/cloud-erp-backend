package com.erp.server.dmp.inout.handler.output.task.mq.aiya;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.aiya.enums.AiyaEnums;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AIYA 2C 出库单 DMP 输出 MQ 任务处理器。
 * 读取 dmp_third_outbound 实体，按 {@link AiyaEnums.OrderStatusEnum} 做状态映射后推送到
 * dmp_platform_outbound_to_wms_topic（具体 Topic/Tag 由 {@code dmp_cfg_mq} 数据库配置驱动，
 * 非本类硬编码），由 PlatformOutboundConsumerService 统一消费处理。
 * <p>
 * 结构与 {@code WegoOutboundRocketMQTaskHandler} 完全等价，仅状态枚举来源不同。
 */
@Service
@Scope("prototype")
public class AiyaOutboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String STORAGE_NAME = "dmp_third_outbound";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertMap = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdOutboundEntity> entityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertMap.entrySet()) {
            if (CollUtil.isNotEmpty(entry.getValue()) && STORAGE_NAME.equals(entry.getKey().getStorageName())) {
                for (BaseEntity v : entry.getValue()) {
                    DmpThirdOutboundEntity e = (DmpThirdOutboundEntity) v;
                    entityMap.put(e.getId(), e);
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeMap = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : changeMap.entrySet()) {
            if (CollUtil.isNotEmpty(entry.getValue()) && STORAGE_NAME.equals(entry.getKey().getStorageName())) {
                for (BaseEntity v : entry.getValue()) {
                    changeIds.add(v.getId());
                }
            }
        }

        Map<String, String> result = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpThirdOutboundEntity entity = entityMap.get(changeId);
            if (entity == null) { continue; }
            PlatformOutboundDTO dto = convert(entity, cfgOutputId);
            if (dto != null) {
                result.put(entity.getId(), JSON.toJSONString(dto));
            }
        }
        return result;
    }

    public PlatformOutboundDTO convert(DmpThirdOutboundEntity entity, String cfgOutputId) {
        if (this.validateDataBlack(entity, cfgOutputId)) { return null; }
        String orderStatus = entity.getOrderStatus();
        String erpOrderStatus = AiyaEnums.OrderStatusEnum.getErpOrderStatus(orderStatus);
        if (StringUtils.isBlank(erpOrderStatus)) { return null; }
        PlatformOutboundDTO dto = BeanUtil.copyProperties(entity, PlatformOutboundDTO.class);
        String sourcePlatform = entity.getSourcePlatform();
        dto.setPlatform(sourcePlatform);
        dto.setProvider(sourcePlatform);
        dto.setOutBoundTime(entity.getDateShipping());
        dto.setOrderStatus(erpOrderStatus);
        dto.setThirdOrderStatus(AiyaEnums.OrderStatusEnum.getName(orderStatus));
        dto.setTrackNo(entity.getTrackingNo());
        return dto;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("orderCode");
    }
}
