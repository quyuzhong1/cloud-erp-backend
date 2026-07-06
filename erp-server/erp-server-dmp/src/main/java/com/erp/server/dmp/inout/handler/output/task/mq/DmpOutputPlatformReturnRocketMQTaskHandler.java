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
import com.erp.server.dmp.service.DmpSoReturnDetailService;
import com.erp.server.dmp.service.DmpSoReturnInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 平台 B2C 退货单通用 MQ 输出：dmp_so_return_info/detail → PlatformReturnOrderDTO → OMS。
 */
@Slf4j
public abstract class DmpOutputPlatformReturnRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final int BATCH_SIZE = 500;
    private static final String STORAGE_RETURN_INFO = "dmp_so_return_info";
    private static final String STORAGE_RETURN_DETAIL = "dmp_so_return_detail";

    @Resource
    private DmpSoReturnDetailService dmpSoReturnDetailService;

    @Resource
    private DmpSoReturnInfoService dmpSoReturnInfoService;

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
            if (STORAGE_RETURN_INFO.equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpSoReturnInfoEntity dmpEntity = (DmpSoReturnInfoEntity) entity;
                    dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
                }
            } else if (STORAGE_RETURN_DETAIL.equals(storageName)) {
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
            if (STORAGE_RETURN_INFO.equals(storageName)) {
                for (BaseEntity entity : value) {
                    changeIds.add(entity.getId());
                }
            } else if (STORAGE_RETURN_DETAIL.equals(storageName)) {
                for (BaseEntity entity : value) {
                    changeIds.add(((DmpSoReturnDetailEntity) entity).getMainId());
                }
            }
        }

        supplementReturnInfos(changeIds, dmpEntityMap);
        supplementReturnDetails(changeIds, dmpEntityMap, dmpDetailEntityMap);

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpSoReturnInfoEntity dmpEntity = dmpEntityMap.get(changeId);
            List<DmpSoReturnDetailEntity> dmpDetailList = dmpDetailEntityMap.get(changeId);
            PlatformReturnOrderDTO orderDTO = convert(dmpEntity, dmpDetailList, cfgOutputId);
            if (orderDTO != null) {
                map.put(changeId, JSON.toJSONString(orderDTO));
            } else {
                log.warn("平台退货MQ输出转换结果为空, changeId: {}, cfgOutputId: {}, hasReturnInfo: {}, detailSize: {}",
                        changeId, cfgOutputId, dmpEntity != null, dmpDetailList == null ? 0 : dmpDetailList.size());
            }
        }
        return map;
    }

    private void supplementReturnInfos(Set<String> changeIds,
                                       Map<String, DmpSoReturnInfoEntity> dmpEntityMap) {
        List<String> missingMainIds = changeIds.stream()
                .filter(StringUtils::isNotBlank)
                .filter(id -> !dmpEntityMap.containsKey(id))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(missingMainIds)) {
            return;
        }
        for (int fromIndex = 0; fromIndex < missingMainIds.size(); fromIndex += BATCH_SIZE) {
            List<String> batchIds = missingMainIds.subList(fromIndex, Math.min(fromIndex + BATCH_SIZE, missingMainIds.size()));
            List<DmpSoReturnInfoEntity> batchInfoList = dmpSoReturnInfoService.lambdaQuery()
                    .in(DmpSoReturnInfoEntity::getId, batchIds)
                    .eq(DmpSoReturnInfoEntity::getIsDeleted, Boolean.FALSE)
                    .list();
            if (CollUtil.isEmpty(batchInfoList)) {
                continue;
            }
            for (DmpSoReturnInfoEntity dmpEntity : batchInfoList) {
                dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
            }
        }
    }

    private void supplementReturnDetails(Set<String> changeIds,
                                         Map<String, DmpSoReturnInfoEntity> dmpEntityMap,
                                         Map<String, List<DmpSoReturnDetailEntity>> dmpDetailEntityMap) {
        List<String> missingMainIds = changeIds.stream()
                .filter(dmpEntityMap::containsKey)
                .filter(id -> CollUtil.isEmpty(dmpDetailEntityMap.get(id)))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(missingMainIds)) {
            return;
        }
        List<DmpSoReturnDetailEntity> detailList = new ArrayList<>();
        for (int fromIndex = 0; fromIndex < missingMainIds.size(); fromIndex += BATCH_SIZE) {
            List<String> batchIds = missingMainIds.subList(fromIndex, Math.min(fromIndex + BATCH_SIZE, missingMainIds.size()));
            List<DmpSoReturnDetailEntity> batchDetailList = dmpSoReturnDetailService.lambdaQuery()
                    .in(DmpSoReturnDetailEntity::getMainId, batchIds)
                    .eq(DmpSoReturnDetailEntity::getIsDeleted, Boolean.FALSE)
                    .list();
            if (CollUtil.isNotEmpty(batchDetailList)) {
                detailList.addAll(batchDetailList);
            }
        }
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        for (DmpSoReturnDetailEntity detailEntity : detailList) {
            dmpDetailEntityMap.computeIfAbsent(detailEntity.getMainId(), key -> new ArrayList<>())
                    .add(detailEntity);
        }
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
