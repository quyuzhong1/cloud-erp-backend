package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeOperatorConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeOperatorConsumerServiceImpl
 * @Description TODO
 * @Date 2024-03-15 14:57
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeeOperatorConsumerServiceImpl implements KingdeeOperatorConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeConsumer(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.KINGDEE_OPERATOR.getCode();
        //业务id
        String  businessId = String.valueOf(map.get("id"));
        //业务编码
        String code = (String) map.get("code");
        //操作项
        String operate = (String) map.get("operate");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_OPERATOR.getCode());

        /**
         * 添加
         */
        if (SyncOperateEnum.OPERATE_ADD.getCode().equals(operate)) {
            operateAddOrUpdate(apiUtils,platformEntity,map,type);
        }

        /**
         * 删除
         */
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            operateDelete(apiUtils,platformEntity,map,operate);
        }
    }

    public void operateAddOrUpdate(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type) {
        //业务id
        String businessId = String.valueOf(map.get("id"));
        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);
        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_FIELD);
        }
        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        }catch (Exception e){
            //未查找到数据，新增数据
            JSONObject firstJson = json;
            JSONUtil.toJsonStr(firstJson);
            JSONUtil.toJsonStr(param);
            //更新数据
            kingdeeCommonService.save(platformEntity,map,apiUtils,json,param,type);
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id"));
        Boolean flag = Boolean.FALSE;
        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(apiUtils,id);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //给修改json对象赋值ID
            KingdeeUtils.makeFieldJson(json, "FSTAFFID", ".", id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.save(platformEntity, map, apiUtils, json, param, type);
        }
    }

    public void operateDelete(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, String operate) {
        //删除
        kingdeeCommonService.delete(apiUtils,platformEntity,map,ApiModuleTypeEnum.KINGDEE_OPERATOR.getCode(),operate);
    }
}
