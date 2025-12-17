package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeCityService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeCountryService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeGlobalAreaService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeOperatorService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeePostService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeProvinceService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysDeptService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysUserInfoService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeUserPostService;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.DictCountryService;
import com.erp.server.sys.service.DictGlobalAreaService;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.erp.server.sys.service.KingdeeOperatorRefPostService;
import com.erp.server.sys.service.KingdeePostService;
import com.erp.server.sys.service.KingdeeUserRefPostService;
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
    private KingdeeUserRefPostService kingdeeUserRefPostService;
    
    @Resource
    private KingdeeDepartmentService kingdeeDepartmentService;
    
    @Resource
    private KingdeePostService kingdeePostService;
    
    @Resource
    private DictCityService dictCityService;
    
    @Resource
    private DictCountryService dictCountryService;
    
    @Resource
    private DictGlobalAreaService dictGlobalAreaService;
    
    @Resource
    private KingdeeOperatorRefPostService kingdeeOperatorRefPostService;

    @Resource
    private SyncKingdeeSysUserInfoService syncKingdeeSysUserInfoService;
    
    @Resource
    private SyncKingdeeUserPostService syncKingdeeUserPostService;
    
    @Resource
    private SyncKingdeeSysDeptService syncKingdeeSysDeptService;
    
    @Resource
    private SyncKingdeePostService syncKingdeePostService;
    
    @Resource
    private SyncKingdeeCityService syncKingdeeCityService;
    
    @Resource
    private SyncKingdeeCountryService syncKingdeeCountryService;
    
    @Resource
    private SyncKingdeeGlobalAreaService syncKingdeeGlobalAreaService;
    
    @Resource
    private SyncKingdeeProvinceService syncKingdeeProvinceService;
    
    @Resource
    private SyncKingdeeOperatorService syncKingdeeOperatorService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
            case SYS_USER_POST:
                resultList = newUserPost(sourceDetailList);
                break;
            case SYS_DEPARTMENT:
            	resultList = newSysDepartment(sourceDetailList);
            	break;
            case SYS_POST:
            	resultList = newSysPost(sourceDetailList);
            	break;
            case PROVINCE_CITY:
            	resultList = newProvinceCity(sourceDetailList);
            	break;
            case COUNTRY:
            	resultList = newCountry(sourceDetailList);
            	break;
            case GLOBAL_AREA:
            	resultList = newGlobalArea(sourceDetailList);
            	break;
            case KINGDEE_OPERATOR:
            	resultList = newKingdeeOperator(sourceDetailList);
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
    
    private Map<String , Map<String, Object>> newUserPost (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<KingdeeUserRefPostEntity> list = kingdeeUserRefPostService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("userPost >>>> 未找到数据！");
    		return resultList;
    	}
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		KingdeeUserRefPostEntity kingdeeUserRefPostEntity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(kingdeeUserRefPostEntity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeUserPostService.newSyncDataToKingdee(kingdeeUserRefPostEntity, syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }
    
    private Map<String , Map<String, Object>> newSysDepartment (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<KingdeeDepartmentEntity> list = kingdeeDepartmentService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("userPost >>>> 未找到数据！");
    		return resultList;
    	}
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		KingdeeDepartmentEntity kingdeeDepartmentEntity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(kingdeeDepartmentEntity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSysDeptService.newSyncDataToKingdee(kingdeeDepartmentEntity, syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }
    
    private Map<String , Map<String, Object>> newSysPost (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<KingdeePostEntity> list = kingdeePostService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("userPost >>>> 未找到数据！");
    		return resultList;
    	}
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		KingdeePostEntity kingdeePostEntity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(kingdeePostEntity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), syncKingdeePostService.newSyncDataToKingdee(kingdeePostEntity, syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }
    
    private Map<String , Map<String, Object>> newProvinceCity (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<DictCityEntity> list = dictCityService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("userPost >>>> 未找到数据！");
    		return resultList;
    	}
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		DictCityEntity dictCityEntity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(dictCityEntity)) {
    			continue;
    		}
    		String type = dictCityEntity.getType();
    		if("province".equals(type)) {
    			resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeProvinceService.newSyncDataToKingdee(dictCityEntity, syncParamDetailDTO.getSyncOperate()));
    		}else {
    			resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCityService.newSyncDataToKingdee(dictCityEntity, syncParamDetailDTO.getSyncOperate()));
    		}
    	}
    	return resultList;
    }
    
    private Map<String , Map<String, Object>> newCountry (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<DictCountryEntity> list = dictCountryService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("userPost >>>> 未找到数据！");
    		return resultList;
    	}
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		DictCountryEntity dictCountryEntity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(dictCountryEntity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCountryService.newSyncDataToKingdee(dictCountryEntity, syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }
    
    private Map<String , Map<String, Object>> newGlobalArea (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<DictGlobalAreaEntity> list = dictGlobalAreaService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("userPost >>>> 未找到数据！");
    		return resultList;
    	}
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		DictGlobalAreaEntity dictGlobalAreaEntity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(dictGlobalAreaEntity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeGlobalAreaService.newSyncDataToKingdee(dictGlobalAreaEntity, syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }
    
    private Map<String , Map<String, Object>> newKingdeeOperator (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<KingdeeOperatorRefPostEntity> list = kingdeeOperatorRefPostService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("userPost >>>> 未找到数据！");
    		return resultList;
    	}
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		KingdeeOperatorRefPostEntity kingdeeOperatorRefPostEntity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(kingdeeOperatorRefPostEntity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeOperatorService.newSyncDataToKingdee(kingdeeOperatorRefPostEntity, syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }
}
