package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysUserInfoService;
import com.erp.server.sys.service.SyncTaskService;
import com.erp.server.sys.service.SysUserInfoService;
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
 * @description: 同步任务实现
 * @date 2023/10/30 11:44
 */
@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private SyncKingdeeSysUserInfoService syncKingdeeSysUserInfoService;

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
            case SYS_USER_INFO:
                resultList = syncSysUser(sourceDetailList);
                break;
            default:
                return;
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
     * @description: 同步用户
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncSysUser (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SysUserInfoEntity> list = sysUserInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSysUser >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity>  resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SysUserInfoEntity sysUserInfoEntity = list.stream().filter(obj -> obj.getUid().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(sysUserInfoEntity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSysUserInfoService.syncDataToKingdee(sysUserInfoEntity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
            return resultList;
        }
        return resultList;
    }

	@Override
	public Map<String, Map<String, Object>> newFindDataSendSyncTask(SyncParamDTO syncParamDTO) {
		List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        switch (sourceType) {
            case SYS_USER_INFO:
                resultList = newSyncSysUser(sourceDetailList);
                break;
            default:
            	break;
        }
		return resultList;
	}
	
	/**
     * @description: 同步用户
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncSysUser (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SysUserInfoEntity> list = sysUserInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSysUser >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SysUserInfoEntity sysUserInfoEntity = list.stream().filter(obj -> {
				return obj.getUid().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(sysUserInfoEntity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSysUserInfoService.newSyncDataToKingdee(sysUserInfoEntity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }
}
