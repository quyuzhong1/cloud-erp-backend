package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformReturnOrderDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
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
 * 平台 B2C 退货单通用 MQ 输出：dmp_so_return_info/detail → PlatformReturnOrderDTO → OMS。
 */
public abstract class DmpOutputPlatformReturnRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps =
                dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoReturnInfoEntity> dmpEntityMap = new HashMap<>();
        Map<String, List<DmpSoReturnDetailEntity>> dmpDetailEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = entry.getKey().getStorageName();
            if ("dmp_so_return_info".equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpSoReturnInfoEntity dmpEntity = (DmpSoReturnInfoEntity) entity;
                    dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
                }
            } else if ("dmp_so_return_detail".equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpSoReturnDetailEntity detailEntity = (DmpSoReturnDetailEntity) entity;
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
            if ("dmp_so_return_info".equals(storageName)) {
                for (BaseEntity entity : value) {
                    changeIds.add(entity.getId());
                }
            } else if ("dmp_so_return_detail".equals(storageName)) {
                for (BaseEntity entity : value) {
                    changeIds.add(((DmpSoReturnDetailEntity) entity).getMainId());
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            PlatformReturnOrderDTO orderDTO = convert(dmpEntityMap.get(changeId), dmpDetailEntityMap.get(changeId), cfgOutputId);
            if (orderDTO != null) {
                map.put(changeId, JSON.toJSONString(orderDTO));
            }
        }
        return map;
    }

    protected PlatformReturnOrderDTO convert(DmpSoReturnInfoEntity dmpEntity,
                                             List<DmpSoReturnDetailEntity> dmpDetailList,
                                             String cfgOutputId) {
        if (dmpEntity == null) {
            return null;
        }
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        if (CollUtil.isEmpty(dmpDetailList)) {
            return null;
        }
        PlatformReturnOrderDTO dto = new PlatformReturnOrderDTO();
        BeanUtils.copyProperties(dmpEntity, dto);
        dto.setUniqueId(buildUniqueId(dmpEntity));
        dto.setPlatformReturnNo(dmpEntity.getThirdCode());
        dto.setPlatformOrderNo(StringUtils.defaultIfBlank(dmpEntity.getPlatformOrderCode(), dmpEntity.getPlatformCode()));
        dto.setReason(StringUtils.defaultIfBlank(dmpEntity.getRemark(), ""));
        dto.setDictPlatform(dmpEntity.getSourceSystem());
        dto.setPlatform(dmpEntity.getSourceSystem());
        dto.setDmpSyncTaskId(cfgOutputId);
        dto.setShopId(dmpEntity.getShopId());
        dto.setBatchNo(dmpEntity.getBatchNo());
        dto.setTrackingNumber(StringUtils.defaultString(dmpEntity.getTrackingNumber()));
        dto.setPlatformStatus(StringUtils.defaultString(dmpEntity.getPlatformStatus()));
        dto.setDetailList(parseReturnDetailList(dmpDetailList));
        return dto;
    }

    protected String buildUniqueId(DmpSoReturnInfoEntity dmpEntity) {
        String thirdCode = StringUtils.defaultString(dmpEntity.getThirdCode());
        String batchNo = StringUtils.trimToEmpty(dmpEntity.getBatchNo());
        if (StringUtils.isBlank(batchNo)) {
            return CharSequenceUtil.format("return_{}", thirdCode);
        }
        return CharSequenceUtil.format("return_{}_{}", thirdCode, batchNo);
    }

    protected List<PlatformReturnOrderDTO.Detail> parseReturnDetailList(List<DmpSoReturnDetailEntity> dmpDetailList) {
        List<PlatformReturnOrderDTO.Detail> resultList = new LinkedList<>();
        for (DmpSoReturnDetailEntity dmpDetailEntity : dmpDetailList) {
            PlatformReturnOrderDTO.Detail detail = new PlatformReturnOrderDTO.Detail();
            detail.setPlatformSkuNo(StringUtils.defaultString(dmpDetailEntity.getSkuNo()));
            detail.setReturnQty(dmpDetailEntity.getQty());
            resultList.add(detail);
        }
        return resultList;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueId");
    }
}
