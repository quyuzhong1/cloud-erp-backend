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
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeOperatorService;
import com.erp.server.sys.service.KingdeeUserRefPostService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import com.erp.server.sys.service.SysPushMsgService;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Lambda
 * @Classname SyncKingdeeOperatorServiceImpl
 * @Description TODO
 * @Date 2024-03-15 14:39
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeOperatorServiceImpl implements SyncKingdeeOperatorService {


    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private KingdeeUserRefPostService kingdeeUserRefPostService;

    @Resource
    private SysAccountingCompanyService sysAccountingCompanyService;
    
    @Resource
    private SysPushMsgService sysPushMsgService;

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Override
    public DmpPushTaskEntity syncDataToKingdee(KingdeeOperatorRefPostEntity entity, String operate) {
    	//生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
    	}
    }

    private DmpPushTaskEntity saveTask(KingdeeOperatorRefPostEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.KINGDEE_OPERATOR.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getId());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.KINGDEE_OPERATOR.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_OPERATOR_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        SysPushMsgEntity sysPushMsgEntity = new SysPushMsgEntity();
    	sysPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
    	sysPushMsgEntity.setSourceType(SourceTypeEnum.KINGDEE_OPERATOR.getCode());
    	sysPushMsgEntity.setSourceId(entity.getId());
    	sysPushMsgEntity.setSourceCode(entity.getId());
    	sysPushMsgEntity.setSyncOperate(operate);
    	sysPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
    	sysPushMsgService.save(sysPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(KingdeeOperatorRefPostEntity entity, String operate) {
		if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException("金蝶业务员表数据不存在");
        }
        Map<String, Object> resultMap = new HashMap<>();
        //业务id
        resultMap.put("id", entity.getId());
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getKingdeeId());
        resultMap.put("operate", operate);
        //业务类型
        resultMap.put("typeCode", entity.getTypeCode());

        List<Map<String, Object>> list = new ArrayList<>(1);
        Map<String, Object> itemMap = new HashMap<>();

        SysAccountingCompanyEntity org = sysAccountingCompanyService.getById(entity.getUseOrgId());
        if (Objects.nonNull(org)) {
            //业务组织
            itemMap.put("useOrgCode", org.getCode());
        }

        KingdeeUserRefPostEntity userPost = kingdeeUserRefPostService.getById(entity.getUserPostId());
        if(Objects.nonNull(userPost)){
            itemMap.put("userPostCode", userPost.getCode());
        }
        list.add(itemMap);
        resultMap.put("list", list);
        return resultMap;
	}
}
