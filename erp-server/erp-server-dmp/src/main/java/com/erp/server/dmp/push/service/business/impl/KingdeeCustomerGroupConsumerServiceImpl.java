package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.utils.FastJsonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeCustomerGroupConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.sdk.third.kingdee.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
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
        //业务id
        String businessId = String.valueOf(map.get("id"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_CUSTOMER.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId, "", "未配置同步字段", type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
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
