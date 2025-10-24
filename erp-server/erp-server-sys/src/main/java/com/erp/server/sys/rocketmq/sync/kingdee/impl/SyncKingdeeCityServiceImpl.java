package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableName;
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
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.SysPushMsgEntity;
import com.erp.model.sys.entity.ThirdpartyRefBusinessEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeCityService;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.SysPushMsgService;
import com.erp.server.sys.service.ThirdpartyRefBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname SyncKingdeeCityServiceImpl
 * @Description TODO
 * @Date 2024-03-20 14:33
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeCityServiceImpl implements SyncKingdeeCityService {
    @Resource
    private ThirdpartyRefBusinessService thirdpartyRefBusinessService;
    @Resource
    private DictCityService dictCityService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private SysPushMsgService sysPushMsgService;

    @Override
    public DmpPushTaskEntity syncDataToKingdee(DictCityEntity entity, String operate) {
    	//生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
    	}
    }

    private DmpPushTaskEntity saveTask(DictCityEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PROVINCE_CITY.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
          DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
          taskFeignDTO.setSourceId(entity.getId());
          taskFeignDTO.setSourceCode(entity.getCode());
          taskFeignDTO.setSourceType(SourceTypeEnum.PROVINCE_CITY.getCode());
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
    	sysPushMsgEntity.setSourceType(SourceTypeEnum.PROVINCE_CITY.getCode());
    	sysPushMsgEntity.setSourceId(entity.getId());
    	sysPushMsgEntity.setSourceCode(entity.getCode());
    	sysPushMsgEntity.setSyncOperate(operate);
    	sysPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
    	sysPushMsgService.save(sysPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(DictCityEntity entity, String operate) {
		Map<String, Object> resultMap = new HashMap<>();
        Boolean isExistParent = true;
        resultMap.put("isExistParent", isExistParent);
        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getKingdeeCode());
        //名称
        resultMap.put("name",entity.getName());
        Class<DictCityEntity> AreaClass = DictCityEntity.class;
        TableName tableName = AreaClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String businessType = tableName.value();
        ThirdpartyRefBusinessEntity thirdpartyRef=  thirdpartyRefBusinessService.getByBusinessId(entity.getId() , businessType);
        String syncKingdeeId="";
        if (Objects.nonNull(thirdpartyRef)) {
            syncKingdeeId = thirdpartyRef.getThirdpartyId();
        }
        //金蝶id
        resultMap.put("syncKingdeeId",syncKingdeeId);
        resultMap.put("operate", operate);
        //模块类型
        Integer moduleType = ApiModuleTypeEnum.PROVINCE_CITY.getCode();
        //辅助资料类型编码
        String fNumber = AssistantDataEnum.CITY.getCode();
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        if(isExistParent){
            DictCityEntity cityEntity = dictCityService.getById(entity.getParentId());
            if (Objects.isNull(cityEntity) || StringUtils.isBlank(cityEntity.getKingdeeCode())) {
                throw new ServiceException(new ApiResult(1, "未找到上级省或者省未同步到金蝶"));
            }
            //上级编码
            resultMap.put("parentCode",cityEntity.getKingdeeCode());

            //上级
            ThirdpartyRefBusinessEntity parentThirdpartyRef=  thirdpartyRefBusinessService.getByBusinessId(cityEntity.getId() , businessType);
            if (Objects.nonNull(parentThirdpartyRef)) {
                resultMap.put("pid",parentThirdpartyRef.getThirdpartyId());
            }
        }
        resultMap.put("moduleType",moduleType);
        resultMap.put("fNumber", fNumber);
        return resultMap;
	}

}
