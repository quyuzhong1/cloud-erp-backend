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
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.enums.AssistantDataEnum;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.SysPushMsgEntity;
import com.erp.model.sys.entity.ThirdpartyRefBusinessEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeCountryService;
import com.erp.server.sys.service.DictGlobalAreaService;
import com.erp.server.sys.service.SysPushMsgService;
import com.erp.server.sys.service.ThirdpartyRefBusinessService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lambda
 * @Classname SyncKingdeeCountryServiceImpl
 * @Description TODO
 * @Date 2024-03-19 17:36
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeCountryServiceImpl implements SyncKingdeeCountryService {
    @Resource
    private ThirdpartyRefBusinessService thirdpartyRefBusinessService;
    @Resource
    private DictGlobalAreaService dictGlobalAreaService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private SysPushMsgService sysPushMsgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(DictCountryEntity entity, String operate) {

        Map<String, Object> resultMap = new HashMap<>();
        Boolean isExistParent = true;
        resultMap.put("isExistParent", isExistParent);
        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getKingdeeCode());
        //名称
        resultMap.put("name",entity.getNameCn());
        ThirdpartyRefBusinessEntity thirdpartyRef=  thirdpartyRefBusinessService.getByBusinessId(entity.getId());
        String syncKingdeeId="";
        if (Objects.nonNull(thirdpartyRef)) {
            syncKingdeeId = thirdpartyRef.getThirdpartyId();
        }
        //金蝶id
        resultMap.put("syncKingdeeId",syncKingdeeId);
        resultMap.put("operate", operate);

        //模块类型
        Integer moduleType = ApiModuleTypeEnum.COUNTRY.getCode();
        //辅助资料类型编码
        String fNumber = AssistantDataEnum.COUNTRY.getCode();
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return saveTask(entity,operate,resultMap);
        }
        if(isExistParent){
            DictGlobalAreaEntity areaEntity = dictGlobalAreaService.getById(entity.getRegionCode());
            if(Objects.isNull(areaEntity)){
                throw new ServiceException(new ApiResult(1,"未找到上级区域"));
            }
            //上级编码
            resultMap.put("parentCode",areaEntity.getKingdeeCode());
            //上级
            ThirdpartyRefBusinessEntity pidThirdpartyRef=  thirdpartyRefBusinessService.getByBusinessId(entity.getRegionCode());
            if (Objects.nonNull(pidThirdpartyRef)) {
                resultMap.put("pid",pidThirdpartyRef.getThirdpartyId());
            }


        }
        resultMap.put("moduleType",moduleType);
        resultMap.put("fNumber", fNumber);
        return saveTask(entity,operate,resultMap);
    }

    private DmpPushTaskEntity saveTask(DictCountryEntity entity, String operate, Map<String, Object> resultMap) {
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
            taskFeignDTO.setSourceCode(entity.getRegionCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.COUNTRY.getCode());
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
    	sysPushMsgEntity.setSourceType(SourceTypeEnum.COUNTRY.getCode());
    	sysPushMsgEntity.setSourceId(entity.getId());
    	sysPushMsgEntity.setSourceCode(entity.getRegionCode());
    	sysPushMsgEntity.setSyncOperate(operate);
    	sysPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
    	sysPushMsgService.save(sysPushMsgEntity);
        
        return null;
    }
}
