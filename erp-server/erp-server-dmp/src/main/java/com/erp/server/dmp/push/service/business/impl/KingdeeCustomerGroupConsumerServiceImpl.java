package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeCustomerGroupConsumerService;
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
 * @Classname KingdeeCustomerGroupConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-01 19:36
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeeCustomerGroupConsumerServiceImpl implements KingdeeCustomerGroupConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void executeConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_GROUP.getCode();

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_CUSTOMER.getCode());

        //操作项
        String operate = (String) map.get("operate");

        /**
         * 审核
         */
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            operateDelete(apiUtils, map);
        } else {
            operateApprove(apiUtils,platformEntity, map,type);
        }

    }

    /**
     * @description: 删除
     * @author Will
     * @date: 2023/9/26 10:35
     * @param apiUtils
     * @param map
     */
    public void operateDelete(KingdeeApiUtils apiUtils,Map<String, Object> map) {
        JSONObject model;
        try {
            model = kingdeeCommonService.queryGroupInfo(apiUtils, (String) map.get("syncKingdeeId"), String.valueOf(map.get("groupName")));
        } catch (Exception e) {
            //未查到则直接返回
            return;
        }
        String id = String.valueOf(model.get("FID"));
        //删除
        kingdeeCommonService.customerGroupDelete(apiUtils,id,"FGroup");
        return;
    }


    /**
     * @description: 审核
     * @author Will
     * @date: 2023/9/26 10:30
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     */
    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {

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
            model = kingdeeCommonService.queryGroupInfo(apiUtils, (String) map.get("syncKingdeeId"), String.valueOf(map.get("groupName")));
        } catch (Exception e) {
            //更新数据
            kingdeeCommonService.customerGroupSaveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            return;
        }
        String id = String.valueOf(model.get("FID"));
        //主单据id
        KingdeeUtils.makeFieldJson(json, "GroupPkId", ".", id);
        StringBuffer allKey = FastJsonUtil.getAllKey(json);
        ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
        param.setNeedUpDateFields(apiFieldList);
        //更新数据
        kingdeeCommonService.customerGroupSaveOrUpdate(platformEntity, map, apiUtils, json, param, type);
    }


}
