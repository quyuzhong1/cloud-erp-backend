package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.service.LogisticsOperateService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 物流平台订单操作记录 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
@Slf4j
@Service
public class LogisticsOperateServiceImpl implements LogisticsOperateService {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private MQProducerService mqProducerService;

    @Override
    public String pullOperateLog(String sourceId,String sourceCode, String businessType, String logisticsPlatform, String status, String requestParamJson, String responseParamJson) {
        DmpPullTaskEntity dmpPullTaskEntity = new DmpPullTaskEntity();
        dmpPullTaskEntity.setSourcePlatformName(PlatformEnum.ERP_TMS.getDesc());
        dmpPullTaskEntity.setSourceType(businessType);
        dmpPullTaskEntity.setSourceId(StringUtils.isBlank(sourceId)? IdWorker.getIdStr() : sourceId);
        dmpPullTaskEntity.setSourceCode(StringUtils.isBlank(sourceCode)? IdWorker.getIdStr() : sourceCode);
        dmpPullTaskEntity.setTargetPlatformName(logisticsPlatform);
        //请求状态（0请求中 1请求成功 2请求失败）
        if (RequestStatusEnums.SUCCESS.getCode().equals(status)) {
            dmpPullTaskEntity.setStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
        } else {
            dmpPullTaskEntity.setStatus(SyncStatusEnum.NO_NEED_SYNC.getCode());
        }
        dmpPullTaskEntity.setMqTopic("");
        dmpPullTaskEntity.setMqTag("");
        dmpPullTaskEntity.setMqData(requestParamJson);
        dmpPullTaskEntity.setReturnMsg(responseParamJson);
        String id = null;
        try {
            id = dmpTaskFeign.saveOrUpdateDmpPullTask(dmpPullTaskEntity);
//            //增加异常预警
//            if (!RequestStatusEnums.SUCCESS.getCode().equals(status)){
//                dmpPullTaskEntity.setId(id);
//                this.sendPullWarnMsg(dmpPullTaskEntity);
//            }

        } catch (Exception e) {
            log.error("saveOrUpdateDmpPullTask:记录操作日志失败");
        }
        return id;
    }

    @Override
    public String pushOperateLog(String sourceId, String sourceCode, String businessType, String logisticsPlatform, String status, String requestParamJson, String responseParamJson, Boolean isSendMsg) {
        DmpPushTaskEntity dmpPushTaskEntity = new DmpPushTaskEntity();
        dmpPushTaskEntity.setSourcePlatformName(PlatformEnum.ERP_TMS.getDesc());
        dmpPushTaskEntity.setSourceType(businessType);
        dmpPushTaskEntity.setSourceId(StringUtils.isBlank(sourceId)?sourceCode:sourceId);
        dmpPushTaskEntity.setSourceCode(sourceCode);
        dmpPushTaskEntity.setTargetPlatformName(logisticsPlatform);
        //请求状态（0请求中 1请求成功 2请求失败）
        if (RequestStatusEnums.SUCCESS.getCode().equals(status)) {
            dmpPushTaskEntity.setStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
        } else {
            dmpPushTaskEntity.setStatus(SyncStatusEnum.NO_NEED_SYNC.getCode());
        }
        dmpPushTaskEntity.setMqTopic("");
        dmpPushTaskEntity.setMqTag("");
        dmpPushTaskEntity.setMqData(requestParamJson);
        dmpPushTaskEntity.setReturnMsg(responseParamJson);
        String id = null;
        try {
            id = dmpTaskFeign.saveOrUpdateDmpPushTask(dmpPushTaskEntity);
            //增加异常预警
//            if (!RequestStatusEnums.SUCCESS.getCode().equals(status) && isSendMsg){
//                dmpPushTaskEntity.setId(id);
//                this.sendPushWarnMsg(dmpPushTaskEntity);
//            }
        } catch (Exception e) {
            log.error("saveOrUpdateDmpPushTask:记录操作日志失败");
        }
        return id;
    }

    @Override
    public void sendPullWarnMsg(DmpPullTaskEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return;
        }
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_TMS);
        warnMsgInfo.setTitle(CharSequenceUtil.format("物流平台【{}】从{}拉取至{}失败",entity.getSourceCode(),entity.getSourcePlatformName(),entity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
        warnMsgInfo.setTableId(entity.getId());
        warnMsgInfo.setKeyInfo(entity.getReturnMsg());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }

    @Override
    public void sendPushWarnMsg(DmpPushTaskEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return;
        }
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_TMS);
        warnMsgInfo.setTitle(CharSequenceUtil.format("物流平台【{}】从{}推送至{}失败",entity.getSourceCode(),entity.getSourcePlatformName(),entity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
        warnMsgInfo.setTableId(entity.getId());
        warnMsgInfo.setKeyInfo(entity.getReturnMsg());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }
}
