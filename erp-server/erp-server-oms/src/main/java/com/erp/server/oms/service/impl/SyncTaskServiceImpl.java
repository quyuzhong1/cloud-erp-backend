package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.entity.*;
import com.erp.server.oms.kingdee.*;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/10/30 11:51
 */
@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SyncKingdeeCustomerService syncKingdeeCustomerInfoService;

    @Resource
    private CustomerContactService customerContactService;

    @Resource
    private SyncKingdeeCustomerContactService syncKingdeeCustomerContactService;

    @Resource
    private CustomerGroupService customerGroupService;

    @Resource
    private SyncKingdeeCustomerGroupService syncKingdeeCustomerGroupService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SyncKingdeeSoService syncKingdeeSoService;

    @Resource
    private SoChangeService soChangeService;

    @Resource
    private SyncKingdeeSoChangeService syncKingdeeSoChangeService;


    @Override
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();

        switch (sourceType) {
            case CUSTOMER_INFO:
                syncCustomerInfo(sourceDetailList);
                return;
            case CUSTOMER_CONTACT:
                syncCustomerContract(sourceDetailList);
                return;
            case CUSTOMER_GROUP:
                syncCustomerGroup(sourceDetailList);
                return;
            case SO_INFO:
                syncSoInfo(sourceDetailList);
                return;
            case SO_CHANGE:
                syncSoChange(sourceDetailList);
                return;
            default:
                return;
        }
    }

    /**
     * @description: 同步客户
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncCustomerInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerInfoEntity> list = customerInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerInfo >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            CustomerInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeCustomerInfoService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步客户联系人
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncCustomerContract (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerContactEntity> list = customerContactService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerContract >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            CustomerContactEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeCustomerContactService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步客户分组
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncCustomerGroup (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerGroupEntity> list = customerGroupService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerGroup >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            CustomerGroupEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeCustomerGroupService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步销售订单
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncSoInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoInfoEntity> list = soInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoInfo >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeSoService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
            //同步订单数据到dmp
            syncKingdeeSoService.syncOrderToDmp(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步销售变更
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncSoChange (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoChangeEntity> list = soChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoChange >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoChangeEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeSoChangeService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }
}
