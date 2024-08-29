package com.erp.server.oms.kingdee.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.entity.CustomerContactEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerContactService;
import com.erp.server.oms.service.CustomerAddressService;
import com.erp.server.oms.service.CustomerInfoService;
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
public class SyncKingdeeCustomerContactServiceImpl implements SyncKingdeeCustomerContactService {
    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private OmsPushMsgService omsPushMsgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(CustomerContactEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //客户联系人
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return saveTask(entity,operate,resultMap);
        }

        //联系人名称
        resultMap.put("person", entity.getPerson());
        //职位
        resultMap.put("position", entity.getPosition());
        //联系电话
        resultMap.put("telNumber", entity.getTelNumber());
        //电子邮箱
        resultMap.put("email", entity.getEmail());
        //是否默认
        resultMap.put("isDefault", entity.getIsDefault());
        //禁用状态
        resultMap.put("disabled", entity.getDisabled());
        //客户地址信息
        List<CustomerAddressDTO.ViewDTO> viewDTOS = customerAddressService.listByMainId(entity.getMainId());
        //客户信息
        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(entity.getMainId());

        //地址编号
        CustomerAddressDTO.ViewDTO viewDTO = viewDTOS.stream().filter(req -> req.getPerson().equals(entity.getPerson())).findFirst().orElse(new CustomerAddressDTO.ViewDTO());
        resultMap.put("addressCode", viewDTO.getCode());
        resultMap.put("address", viewDTO.getAddress());
        //客户编号
        resultMap.put("customerCode", customerInfoEntity.getCode());

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
    private DmpPushTaskEntity saveTask (CustomerContactEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, settingEnum.getKey())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.CUSTOMER_CONTACT.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_CUSTOMER_CONTACT_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return  dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
    	
    	OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.CUSTOMER_CONTACT.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
        
        return null;
    }
}
