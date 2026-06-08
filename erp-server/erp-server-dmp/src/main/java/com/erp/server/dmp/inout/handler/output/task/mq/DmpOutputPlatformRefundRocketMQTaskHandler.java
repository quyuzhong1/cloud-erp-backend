package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformRefundOrderDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoRefundDetailEntity;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 平台 B2C 退款单通用 MQ 输出：dmp_so_refund_info/detail → PlatformRefundOrderDTO → OMS。
 */
public abstract class DmpOutputPlatformRefundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps =
                dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoRefundInfoEntity> dmpEntityMap = new HashMap<>();
        Map<String, List<DmpSoRefundDetailEntity>> dmpDetailEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = entry.getKey().getStorageName();
            if ("dmp_so_refund_info".equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpSoRefundInfoEntity dmpEntity = (DmpSoRefundInfoEntity) entity;
                    dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
                }
            } else if ("dmp_so_refund_detail".equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpSoRefundDetailEntity detailEntity = (DmpSoRefundDetailEntity) entity;
                    dmpDetailEntityMap.computeIfAbsent(detailEntity.getMainId(), key -> new ArrayList<>())
                            .add(detailEntity);
                }
            }
        }

        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry :
                dmpRequest.getChangeConvertInputDmpBaseEntityListMaps().entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = entry.getKey().getStorageName();
            if ("dmp_so_refund_info".equals(storageName)) {
                for (BaseEntity entity : value) {
                    changeIds.add(entity.getId());
                }
            } else if ("dmp_so_refund_detail".equals(storageName)) {
                for (BaseEntity entity : value) {
                    changeIds.add(((DmpSoRefundDetailEntity) entity).getMainId());
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            PlatformRefundOrderDTO orderDTO = convert(dmpEntityMap.get(changeId), dmpDetailEntityMap.get(changeId), cfgOutputId);
            if (orderDTO != null) {
                map.put(changeId, JSON.toJSONString(orderDTO));
            }
        }
        return map;
    }

    protected PlatformRefundOrderDTO convert(DmpSoRefundInfoEntity dmpEntity,
                                             List<DmpSoRefundDetailEntity> dmpDetailList,
                                             String cfgOutputId) {
        if (dmpEntity == null || CollUtil.isEmpty(dmpDetailList)) {
            return null;
        }
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        if (!acceptRefund(dmpEntity)) {
            return null;
        }

        PlatformRefundOrderDTO dto = new PlatformRefundOrderDTO();
        BeanUtils.copyProperties(dmpEntity, dto);
        dto.setUniqueId(dmpEntity.getThirdCode());
        dto.setPlatformRefundNo(dmpEntity.getThirdCode());
        dto.setPlatformOrderNo(StringUtils.defaultIfBlank(dmpEntity.getPlatformOrderCode(), dmpEntity.getPlatformCode()));
        dto.setRemark(StringUtils.defaultIfBlank(dmpEntity.getRemark(), dmpEntity.getReason()));
        String platform = StringUtils.defaultIfBlank(dmpEntity.getSourcePlatform(), dmpEntity.getSourceSystem());
        dto.setDictPlatform(platform);
        dto.setPlatform(platform);
        dto.setShopId(dmpEntity.getShopId());
        dto.setRefundAmount(dmpEntity.getAmount());
        dto.setCurrency(dmpEntity.getCurrencyCode());
        dto.setDmpSyncTaskId(cfgOutputId);
        dto.setDetailList(parseRefundDetailList(dmpDetailList));
        return dto;
    }

    protected boolean acceptRefund(DmpSoRefundInfoEntity dmpEntity) {
        return true;
    }

    protected List<PlatformRefundOrderDTO.Detail> parseRefundDetailList(List<DmpSoRefundDetailEntity> dmpDetailList) {
        List<PlatformRefundOrderDTO.Detail> resultList = new LinkedList<>();
        for (DmpSoRefundDetailEntity dmpDetailEntity : dmpDetailList) {
            PlatformRefundOrderDTO.Detail detail = new PlatformRefundOrderDTO.Detail();
            detail.setPlatformSkuNo(StringUtils.defaultString(dmpDetailEntity.getSkuNo()));
            detail.setRefundQty(dmpDetailEntity.getQty());
            resultList.add(detail);
        }
        return resultList;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueId");
    }
}
