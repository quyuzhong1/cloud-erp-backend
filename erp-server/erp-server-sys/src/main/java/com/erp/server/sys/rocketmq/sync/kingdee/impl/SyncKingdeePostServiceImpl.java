package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeePostService;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import com.erp.server.sys.service.SysPushMsgService;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname SyncKingdeePostServiceImpl
 * @Description TODO
 * @Date 2024-03-13 15:50
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeePostServiceImpl implements SyncKingdeePostService {


    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SysPushMsgService sysPushMsgService;
    
    @Resource
    private SysAccountingCompanyService sysAccountingCompanyService;
    
    @Resource
    private KingdeeDepartmentService kingdeeDepartmentService;
    
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public DmpPushTaskEntity syncDataToKingdee(KingdeePostEntity entity, String operate) {
    	//生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
    	}

    }

    /**
     * 生成任务
     * @description
     * @return
     * @date 2024-03-13 15:53
     * @author Lambda
     */
    private DmpPushTaskEntity saveTask(KingdeePostEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.SYS_POST.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getId());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SYS_POST.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SYS_POST_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        SysPushMsgEntity sysPushMsgEntity = new SysPushMsgEntity();
    	sysPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
    	sysPushMsgEntity.setSourceType(SourceTypeEnum.SYS_POST.getCode());
    	sysPushMsgEntity.setSourceId(entity.getId());
    	sysPushMsgEntity.setSourceCode(entity.getId());
    	sysPushMsgEntity.setSyncOperate(operate);
    	sysPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
    	sysPushMsgService.save(sysPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(KingdeePostEntity entity, String operate) {
		if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException("金蝶岗位表不存在");
        }
        Map<String, Object> resultMap = new HashMap<>();
        //业务id
        resultMap.put("id",entity.getId());
        //名称
        resultMap.put("name",entity.getName());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getKingdeeId());
        resultMap.put("operate", operate);
        String useOrgCode = entity.getUseOrgCode();
        if(StringUtils.isBlank(useOrgCode)) {
        	String useOrgId = entity.getUseOrgId();
        	if(StringUtils.isNotBlank(useOrgId)) {
        		SysAccountingCompanyEntity orgInfo = sysAccountingCompanyService.getById(useOrgId);
        		if(orgInfo != null) {
        			useOrgCode = orgInfo.getCode();
        		}
        	}
        }
        resultMap.put("createOrgCode", useOrgCode);
        resultMap.put("useOrgCode",useOrgCode);
        String kingdeeDeptCode = entity.getKingdeeDeptCode();
        if(StringUtils.isBlank(kingdeeDeptCode)) {
        	String kingdeeDeptId = entity.getKingdeeDeptId();
        	if(StringUtils.isNotBlank(kingdeeDeptId)) {
        		KingdeeDepartmentEntity kingdeeDept = kingdeeDepartmentService.getById(kingdeeDeptId);
        		if(kingdeeDept != null) {
        			kingdeeDeptCode = kingdeeDept.getKingdeeDeptCode();
        		}
        	}
        }
		resultMap.put("deptCode", kingdeeDeptCode);
        return resultMap;
	}
}
