package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FastJsonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeTransferInfoConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
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
 * @Classname KingdeeTransferInfoConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-02 9:49
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeeTransferInfoConsumerServiceImpl implements KingdeeTransferInfoConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.TRANSFER_INFO.getCode();

        //业务id
        String  businessId = String.valueOf(map.get("id"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_TRANSFERDIRECT.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(),type);


        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","未配置同步字段",type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        Boolean isAdd = Boolean.FALSE;
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
        } catch (Exception e) {
            //新增数据
            Boolean isSaveOrUpdate = kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils , json, param,type);
            if (!isSaveOrUpdate) {
                return;
            }
            isAdd = Boolean.TRUE;
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
            //执行操作
            operate (platformEntity,map,apiUtils,model,json,isAdd);
            return;
        }
        //执行操作
        operate (platformEntity,map,apiUtils,model,json,isAdd);
    }

    /**
     * 直接调拨单操作
     */
    public void operate (PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils,JSONObject model,JSONObject json,Boolean isAdd) {
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id")) ;
        //操作项
        String operate = (String) map.get("operate");
        //作废
        if (SyncOperateEnum.OPERATE_INVALID.getCode().equals(operate)) {
            operateInvalid(platformEntity,map,apiUtils,id,documentStatus);
        }
        //反审核
        if (SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            operateDisapprove(platformEntity,map,apiUtils,id,documentStatus);
        }
        //审核
        if (SyncOperateEnum.OPERATE_APPROVE.getCode().equals(operate) && !isAdd) {
            operateApprove(platformEntity,map,apiUtils,id,documentStatus,json);
        }
    }


    /**
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @description: 作废
     * @author Will
     * @date: 2023/5/24 17:57
     */
    public void operateInvalid (PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id,String documentStatus){
        if (KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus)) {
            //反审核
            Boolean unAudit = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, ApiModuleTypeEnum.TRANSFER_INFO.getCode());
            if (!unAudit) {
                return;
            }
        }
        //作废
        kingdeeCommonService.excuteOperation(apiUtils, platformEntity, map, ApiModuleTypeEnum.TRANSFER_INFO.getCode(),(String) map.get("code"),(String) map.get("operate"));
        return;
    }

    /**
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @description: 反审核
     * @author Will
     * @date: 2023/5/24 17:57
     */
    public void operateDisapprove (PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id,String documentStatus){
        if (KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus)) {
            //提交审核
            Boolean submit = kingdeeCommonService.submit(platformEntity, map, apiUtils, id, ApiModuleTypeEnum.TRANSFER_INFO.getCode());
            if (!submit) {
                return;
            }
        }
        //反审核
        kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, ApiModuleTypeEnum.TRANSFER_INFO.getCode());
        return;
    }


    /**
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @description: 审核
     * @author Will
     * @date: 2023/5/24 18:10
     */
    public void operateApprove (PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id,String documentStatus,JSONObject json){
        //查找到数据后，判断其审核状态
        Boolean flag = Boolean.FALSE;
        SaveParam param = new SaveParam(json);
        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, ApiModuleTypeEnum.TRANSFER_INFO.getCode());
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //给修改json对象赋值ID
            KingdeeUtils.makeFieldJson(json, "FId", ".", id);
            //更新数据不能传入库组织
            json.remove("FStockOrgId");
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, ApiModuleTypeEnum.TRANSFER_INFO.getCode());
        }
    }
}
