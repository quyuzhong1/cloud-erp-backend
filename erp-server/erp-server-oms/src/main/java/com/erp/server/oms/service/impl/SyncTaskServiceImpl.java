package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.oms.kingdee.*;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        switch (sourceType) {
            case CUSTOMER_INFO:
                resultList = syncCustomerInfo(sourceDetailList);
                break;
            case CUSTOMER_CONTACT:
                resultList = syncCustomerContract(sourceDetailList);
                break;
            case CUSTOMER_GROUP:
                resultList = syncCustomerGroup(sourceDetailList);
                break;
            case SO_INFO:
                resultList = syncSoInfo(sourceDetailList);
                break;
            case SO_CHANGE:
                resultList = syncSoChange(sourceDetailList);
                break;
            default:
                break;
        }
        //推送金蝶
        List<DmpPushTaskEntity> finalResultList = resultList;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(finalResultList);
            }
        });
    }

    /**
     * @description: 同步客户
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncCustomerInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerInfoEntity> list = customerInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerInfo >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            CustomerInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            List<DmpPushTaskEntity> dmpPushTaskList = syncKingdeeCustomerInfoService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.addAll(dmpPushTaskList);
        }
        return resultList;
    }

    /**
     * @description: 同步客户联系人
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncCustomerContract (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerContactEntity> list = customerContactService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerContract >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            CustomerContactEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCustomerContactService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * @description: 同步客户分组
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncCustomerGroup (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerGroupEntity> list = customerGroupService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerGroup >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            CustomerGroupEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCustomerGroupService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * @description: 同步销售订单
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncSoInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoInfoEntity> list = soInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoInfo >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
            //同步订单数据到dmp
            syncKingdeeSoService.syncOrderToDmp(entity,syncParamDetailDTO.getSyncOperate());
        }
        return resultList;
    }

    /**
     * @description: 同步销售变更
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncSoChange (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoChangeEntity> list = soChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoChange >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoChangeEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoChangeService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

	@Override
	public Map<String , Map<String, Object>> newFindDataSendSyncTask(SyncParamDTO syncParamDTO) {
		List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        switch (sourceType) {
            case CUSTOMER_INFO:
                resultList = newSyncCustomerInfo(sourceDetailList);
                break;
            case CUSTOMER_CONTACT:
                resultList = newSyncCustomerContract(sourceDetailList);
                break;
            case CUSTOMER_GROUP:
                resultList = newSyncCustomerGroup(sourceDetailList);
                break;
            case SO_INFO:
                resultList = newSyncSoInfo(sourceDetailList);
                break;
            case SO_CHANGE:
                resultList = newSyncSoChange(sourceDetailList);
                break;
            default:
                break;
        }
		return resultList;
	}
	
	/**
     * @description: 同步客户
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncCustomerInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerInfoEntity> list = customerInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            CustomerInfoEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerInfoService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步客户联系人
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncCustomerContract (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerContactEntity> list = customerContactService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerContract >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            CustomerContactEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerContactService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步客户分组
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncCustomerGroup (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerGroupEntity> list = customerGroupService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerGroup >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            CustomerGroupEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerGroupService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步销售订单
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncSoInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoInfoEntity> list = soInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SoInfoEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步销售变更
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncSoChange (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoChangeEntity> list = soChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoChange >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SoChangeEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoChangeService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }
}
