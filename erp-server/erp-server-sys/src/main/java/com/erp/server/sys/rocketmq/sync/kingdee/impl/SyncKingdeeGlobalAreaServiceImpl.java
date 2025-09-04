package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.enums.AssistantDataEnum;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.SysPushMsgEntity;
import com.erp.model.sys.entity.ThirdpartyRefBusinessEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeGlobalAreaService;
import com.erp.server.sys.service.SysPushMsgService;
import com.erp.server.sys.service.ThirdpartyRefBusinessService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lambda
 * @Classname SyncKingdeeGlobalAreaServiceImpl
 * @Description TODO
 * @Date 2024-03-19 14:39
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeGlobalAreaServiceImpl implements SyncKingdeeGlobalAreaService {

    @Resource
    private ThirdpartyRefBusinessService thirdpartyRefBusinessService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private SysPushMsgService sysPushMsgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(DictGlobalAreaEntity entity, String operate) {
    	//生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
    	}
    }

    private DmpPushTaskEntity saveTask(DictGlobalAreaEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.GLOBAL_AREA.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getRegionCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.GLOBAL_AREA.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_ASSISTANT_DATA_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }
    	
    	SysPushMsgEntity sysPushMsgEntity = new SysPushMsgEntity();
    	sysPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
    	sysPushMsgEntity.setSourceType(SourceTypeEnum.GLOBAL_AREA.getCode());
    	sysPushMsgEntity.setSourceId(entity.getId());
    	sysPushMsgEntity.setSourceCode(entity.getRegionCode());
    	sysPushMsgEntity.setSyncOperate(operate);
    	sysPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
    	sysPushMsgService.save(sysPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(DictGlobalAreaEntity entity, String operate) {
		Map<String, Object> resultMap = new HashMap<>();
        boolean isExistParent = false;
        resultMap.put("isExistParent", isExistParent);
        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getKingdeeCode());
        //名称
        resultMap.put("name",entity.getRegionName());
        ThirdpartyRefBusinessEntity thirdpartyRef=  thirdpartyRefBusinessService.getByBusinessId(entity.getId());
        String syncKingdeeId="";
        if (Objects.nonNull(thirdpartyRef)) {
            syncKingdeeId = thirdpartyRef.getThirdpartyId();
        }
        //金蝶id
        resultMap.put("syncKingdeeId",syncKingdeeId);
        resultMap.put("operate", operate);
            //模块类型
        Integer moduleType = ApiModuleTypeEnum.GLOBAL_AREA.getCode();
        //辅助资料类型编码
        String fNumber = AssistantDataEnum.GLOBAL_AREA.getCode();
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        resultMap.put("moduleType",moduleType);
        resultMap.put("fNumber", fNumber);
        return resultMap;
	}
}
