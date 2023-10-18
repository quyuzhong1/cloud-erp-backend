package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FastJsonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.server.dmp.push.service.business.KingdeeSoConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeSoConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-02 9:04
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeeSoConsumerServiceImpl implements KingdeeSoConsumerService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private MQProducerService mqProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.SO_INFO.getCode();
        //业务id
        String businessId = String.valueOf(map.get("id"));
        //业务编码
        String code = (String) map.get("code");
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_SALEORDER.getCode());

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
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {
            //更新数据
            Boolean flag = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (!flag) {
                sendWarnMsg(businessId);
            }
            return;
        }

        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id"));

        KingdeeUtils.makeFieldJson(json,"FID",".",id);
        StringBuffer allUpdateKey = FastJsonUtil.getAllKey(json);
        ArrayList<String> apiUpdateFieldList = (ArrayList)Arrays.stream(allUpdateKey.toString().split(",")).collect(Collectors.toList());
        param.setNeedUpDateFields(apiUpdateFieldList);

        //整单折扣
     //   BigDecimal discountAmount = (BigDecimal) map.getOrDefault("discountAmount", BigDecimal.ZERO);
        //更新数据
        Boolean flag = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
        if (!flag) {
            sendWarnMsg(businessId);
        }


        //操作项
        String operate = (String) map.get("operate");
        if (SyncOperateEnum.OPERATE_INVALID.getCode().equals(operate)) {

            //作废
            kingdeeCommonService.excuteOperation(apiUtils, platformEntity, map, type, code, operate);
            return;
        }
        if (SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            //审核中或已审核则要先反审
            if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
                flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
            } else {
                kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId, "", "已经反审核", type, ApiSendStatusEnum.SUCCESS.getCode());
            }
            return;
        }
        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //给修改json对象赋值ID
            KingdeeUtils.makeFieldJson(json, "FId", ".", id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
        }

    }

    /***
     * 发送预警信息
     * @author yl
     * @date 2023-06-14 9:50
     * @param businessId
     * @return void
     */
    public void sendWarnMsg(String businessId) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("销售订单同步");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
        warnMsgInfo.setTitle("销售订单同步失败");
        warnMsgInfo.setTableName("so_info");
        warnMsgInfo.setTableId(businessId);
        warnMsgInfo.setKeyInfo("");
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }
}
