package com.erp.server.oms.kingdee.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.entity.CustomerGroupEntity;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerGroupService;
import com.erp.server.oms.service.OmsPushMsgService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

/**
 * 同步客户到金蝶
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeCustomerGroupServiceImpl implements SyncKingdeeCustomerGroupService {

    @Resource
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private OmsPushMsgService omsPushMsgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(CustomerGroupEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //金蝶id
        if (StringUtils.isNotBlank(entity.getSyncKingdeeId())) {
            resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        }
        //业务id
        resultMap.put("id", entity.getId());
        //分组名称
        resultMap.put("groupName", entity.getName());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //生成任务
       return saveTask(entity,operate,resultMap);
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (CustomerGroupEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, settingEnum.getKey())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
          DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
          taskFeignDTO.setSourceId(entity.getId());
          taskFeignDTO.setSourceCode(entity.getName());
          taskFeignDTO.setSourceType(SourceTypeEnum.CUSTOMER_GROUP.getCode());
          taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
          taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_CUSTOMER_GROUP_TAG.getName());
          taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
          taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
          taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
          taskFeignDTO.setSyncOperate(operate);
          return dmpMqFeign.saveTask(taskFeignDTO);
        }
    	
    	OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getName());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.CUSTOMER_GROUP.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
        
        return null;
    }
}
