package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeBomInfoService;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeCategoryService;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeProductDetailService;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 同步任务实现
 * @date 2023/10/30 10:43
 */
@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private SyncKingdeeCategoryService syncKingdeeCategoryService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private SyncKingdeeProductDetailService syncKingdeeProductDetailService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductBomHistoryService productBomHistoryService;

    @Resource
    private SyncKingdeeBomInfoService syncKingdeeBomInfoService;

    @Override
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();

        switch (sourceType) {
            case BASIC_CATEGORY:
                syncCategory(sourceDetailList);
                return;
            case PRODUCT_DETAIL:
                syncProductDetail(sourceDetailList);
                return;
            case PRODUCT_BOM_INFO:
                syncBomInfo(sourceDetailList);
                return;
        }
    }

    /**
     * @description: 同步产品分类
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncCategory (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<BasicCategoryEntity> list = basicCategoryService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCategory >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            BasicCategoryEntity basicCategoryEntity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(basicCategoryEntity)) {
                continue;
            }
            syncKingdeeCategoryService.syncDataToKingdee(basicCategoryEntity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步产品
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncProductDetail (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ProductDetailEntity> list = productDetailService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncProductDetail >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            ProductDetailEntity poroductDetailEntity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(poroductDetailEntity)) {
                continue;
            }
            syncKingdeeProductDetailService.syncDataToKingdee(poroductDetailEntity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步BOM
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncBomInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ProductBomHistoryEntity> list = productBomHistoryService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncBomInfo >>>> 未找到数据！");
            return;
        }
        List<String> bomIdList = list.stream().map(ProductBomHistoryEntity::getBomId).collect(Collectors.toList());
        List<BomInfoEntity> bomList = bomInfoService.listByIds(bomIdList);
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            ProductBomHistoryEntity productBomHistoryEntity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(productBomHistoryEntity)) {
                continue;
            }
            BomInfoEntity bomInfoEntity = bomList.stream().filter(obj -> obj.getId().equals(productBomHistoryEntity.getBomId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bomInfoEntity)) {
                continue;
            }
            syncKingdeeBomInfoService.syncDataToKingdee(bomInfoEntity,syncParamDetailDTO.getSyncOperate());
        }
    }

}
