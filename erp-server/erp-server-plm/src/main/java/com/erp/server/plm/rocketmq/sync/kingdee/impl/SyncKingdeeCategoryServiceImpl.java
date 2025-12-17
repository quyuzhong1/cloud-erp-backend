package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
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
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.PlmPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeCategoryService;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.PlmPushMsgService;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeeCategoryServiceImpl implements SyncKingdeeCategoryService {

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private BasicCategoryService basicCategoryService;
    
    @Resource
    private PlmPushMsgService plmPushMsgService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(BasicCategoryEntity entity, String operate) {
        //生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
    	}
    }


    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (BasicCategoryEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.BASIC_CATEGORY.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
          DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
          taskFeignDTO.setSourceId(entity.getId());
          taskFeignDTO.setSourceCode(entity.getCode());
          taskFeignDTO.setSourceType(SourceTypeEnum.BASIC_CATEGORY.getCode());
          taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
          taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_ASSISTANT_DATA_TAG.getName());
          taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
          taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
          taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
          taskFeignDTO.setSyncOperate(operate);
          return dmpMqFeign.saveTask(taskFeignDTO);
        }
        
        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.BASIC_CATEGORY.getCode());
        plmPushMsgEntity.setSourceId(entity.getId());
        plmPushMsgEntity.setSourceCode(entity.getCode());
        plmPushMsgEntity.setSyncOperate(operate);
        plmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        plmPushMsgService.save(plmPushMsgEntity);
        
        return null;
    }


	@Override
	public Map<String, Object> newSyncDataToKingdee(BasicCategoryEntity entity, String operate) {
		Map<String, Object> resultMap = new HashMap<>();

        //是否存在上级
        boolean isExistParent = !MathUtil.ZERO.toString().equals(entity.getPid());
        resultMap.put("isExistParent", isExistParent);
        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //名称
        resultMap.put("name",entity.getName());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //模块类型
        Integer moduleType = ApiModuleTypeEnum.ONE_LEVEL_CATEGORY.getCode();
        //辅助资料类型编码
        String fNumber = AssistantDataEnum.ONE_LEVEL_CATEGORY.getCode();

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }

        //二级分类
        if (isExistParent) {
            moduleType = ApiModuleTypeEnum.SECOND_LEVEL_CATEGORY.getCode();
            fNumber = AssistantDataEnum.SECOND_LEVEL_CATEGORY.getCode();
            //查询上级分类编码
            BasicCategoryEntity parent = basicCategoryService.lambdaQuery().eq(BasicCategoryEntity::getId, entity.getPid()).one();
            if (ObjectUtils.isEmpty(parent)) {
                log.error("未找到上级分类，pid = {}",entity.getPid());
              throw new ServiceException(new ApiResult(1,"未找到上级分类"));
            }
            //上级编码
            resultMap.put("parentCode",parent.getCode());
            //二级编码
            resultMap.put("code",parent.getCode().concat(entity.getCode()));
        }
        resultMap.put("moduleType",moduleType);
        resultMap.put("fNumber", fNumber);
        return resultMap;
	}
}
