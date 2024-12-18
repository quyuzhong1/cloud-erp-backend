package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.server.tms.service.*;
import com.erp.server.tms.sync.SyncLogisticsBillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {
    @Resource
    private LogisticsBillService logisticsBillService;

    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    @Resource
    private SyncLogisticsBillService syncLogisticsBillService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Override
    public Map<String, Map<String, Object>> newFindDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        switch (sourceType) {
            case SDY_LOGISTICS_BILL:
                resultList = newSyncLogisticsBill(sourceDetailList);
                break;
            default:
                break;
        }
        return resultList;
    }

    /**
     * 运单同步数帝云
     * @param sourceDetailList
     */
    /**
     * 销售退货入库单
     * @param sourceDetailList
     * @return
     */
    private Map<String ,Map<String, Object>> newSyncLogisticsBill(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        //同步数帝云是详情级别同步，所以查询详情
        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            log.error("newSyncLogisticsBill >>>> 未找到数据！");
            return resultList;
        }
        List<String> billIds = detailEntityList.stream().map(LogisticsBillDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> entityList = logisticsBillService.listByIds(billIds);

        List<String> channelIds = entityList.stream().map(req -> req.getChannelId()).distinct().collect(Collectors.toList());
        List<LogisticsChannelEntity> logisticsChannelEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(logisticsChannelEntities)) {
            logisticsChannelEntities = logisticsChannelService.listByIds(channelIds);
        }

        List<String> supplierIds = logisticsChannelEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());

        List<LogisticsSupplierEntity> logisticsSupplierEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(logisticsSupplierEntities)) {
            logisticsSupplierEntities = logisticsSupplierService.listByIds(supplierIds);
        }

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            LogisticsBillDetailEntity detailEntity = detailEntityList.stream().filter(req -> req.getId().equals(sourceId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                continue;
            }
            LogisticsBillEntity entity = entityList.stream().filter(req -> req.getId().equals(detailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncLogisticsBillService.syncDataToSdyFieldHandler(entity, detailEntity, syncParamDetailDTO.getSyncOperate(), logisticsChannelEntities, logisticsSupplierEntities));
        }
        return resultList;
    }

}
