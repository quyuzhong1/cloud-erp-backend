package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FastJsonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeCustomerContactConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.sdk.third.kingdee.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeCustomerContactConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-01 19:20
 * @Created by yl
 */
@Slf4j
@Service
public class KingdeeCustomerContactConsumerServiceImpl implements KingdeeCustomerContactConsumerService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeConsumer(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_CONTACT.getCode();

        //业务id
        String  businessId = String.valueOf(map.get("id"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_COMMONCONTACT.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","未配置同步字段",type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }


        //地址编号
        String addressCode = String.valueOf(map.get("addressCode"));
        //联系人金蝶id
        String syncKingdeeId = String.valueOf(map.get("syncKingdeeId"));
        //联系人编号
        String code = String.valueOf(map.get("code"));
        //如果所有编码都没有无法同步，需要手动设置好编号
        if (StringUtils.isBlank(addressCode) || (StringUtils.isBlank(syncKingdeeId) && StringUtils.isBlank(code))) {
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","地址编码或联系人编号是空，同步金蝶失败，请手动维护数据",type, ApiSendStatusEnum.FAILURE.getCode());
        }

        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(), map);
        } catch (Exception e) {
            //更新数据
            Boolean flag = kingdeeCommonService.saveOrUpdateCustomerContact(platformEntity, map, apiUtils, json, param, type);
            if (flag) {
                //如果新增是禁用状态需要调用禁用接口
                if (Boolean.valueOf(map.get("disabled").toString())) {
                    //启用、禁用
                    excuteOperation(apiUtils,platformEntity,map,type);
                }
            }
            return;
        }
        String id = String.valueOf(model.get("Id"));
        String forbidStatus = String.valueOf(model.get("ForbidStatus")) ;
        StringBuffer allKey = FastJsonUtil.getAllKey(json);
        Boolean flag = Boolean.FALSE;
        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");

        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //已禁用不
            if (forbidStatus.equals("B")) {
                return;
            }
            flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
        }
        //给修改json对象赋值ID
        KingdeeUtils.makeFieldJson(json,"FCONTACTID",".", id);
        ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
        param.setNeedUpDateFields(apiFieldList);
        //更新数据
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            kingdeeCommonService.saveOrUpdateCustomerContact(platformEntity,map,apiUtils,json,param,type);
            if ((forbidStatus.equals("B") && Boolean.valueOf(map.get("disabled").toString()) == Boolean.FALSE) || (forbidStatus.equals("A") && Boolean.valueOf(map.get("disabled").toString()))) {
                //启用、禁用
                excuteOperation(apiUtils,platformEntity,map,type);
            }
        }
    }


    /**
     * 启用、禁用
     */
    public void excuteOperation(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //仓库状态 true禁用,false启用
        Object disabled = map.get("disabled");
        if (ObjectUtils.isEmpty(disabled)) {
            return;
        }

        String code = (String) map.get("code");
        String operate = null;
        //启用
        if (!(Boolean) disabled) {
            operate = SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode();
        }
        //禁用
        if ((Boolean) disabled) {
            operate = SyncKingdeeOperateEnum.OPERATE_DISABLE.getCode();
        }
        if (StringUtils.isNotBlank(operate)) {
            kingdeeCommonService.excuteOperation(apiUtils,platformEntity,map,type,code,operate);
        }
    }
}
