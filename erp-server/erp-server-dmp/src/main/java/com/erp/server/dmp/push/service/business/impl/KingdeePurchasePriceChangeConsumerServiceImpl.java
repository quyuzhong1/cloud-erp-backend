package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.KingdeeParamDTO;
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
import com.erp.server.dmp.push.service.business.KingdeePurchasePriceChangeConsumerService;
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
 * @Classname KingdeePurchasePriceChangeConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-01 20:33
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeePurchasePriceChangeConsumerServiceImpl implements KingdeePurchasePriceChangeConsumerService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode();

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_PAT.getCode());

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
        } catch (Exception e) {

            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            return;
        }

        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");

        String id = String.valueOf(model.get("Id"));
        Boolean flag = Boolean.FALSE;

        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(apiUtils, id);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //主单据id
            KingdeeUtils.makeFieldJson(json, "FId", ".", id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
        }
    }


}
