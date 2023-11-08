package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsOrderOperateLogEntity;
import com.erp.server.tms.mapper.LogisticsOrderOperateLogMapper;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsOrderOperateLogDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.print.DocFlavor;

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
public class LogisticsOrderOperateLogServiceImpl extends SuperServiceImpl<LogisticsOrderOperateLogMapper, LogisticsOrderOperateLogEntity> implements LogisticsOrderOperateLogService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsOrderOperateLogDTO.AddDTO addDTO) {
        LogisticsOrderOperateLogEntity logisticsOrderOperateLogEntity = new LogisticsOrderOperateLogEntity();
        BeanMapperUtils.copy(addDTO, logisticsOrderOperateLogEntity);

        // 数据处理
        handleData(logisticsOrderOperateLogEntity);

        log.info("开始新增物流平台订单操作记录");
        boolean save = super.save(logisticsOrderOperateLogEntity);
        if (!save) {
            throw new ServiceException("物流平台订单操作记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流平台订单操作记录", logisticsOrderOperateLogEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsOrderOperateLogEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsOrderOperateLogEntity.getId(), logisticsOrderOperateLogEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsOrderOperateLogDTO.UpdateDTO updateDTO) {
        LogisticsOrderOperateLogEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流平台订单操作记录"));
        LogisticsOrderOperateLogEntity logisticsOrderOperateLogEntity = BeanMapperUtils.map(LogisticsOrderOperateLogEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsOrderOperateLogEntity);
        log.info("编辑 开始修改物流平台订单操作记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsOrderOperateLogEntity);
        if (!save) {
            throw new ServiceException("物流平台订单操作记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录物流平台订单操作记录日志数据，id：【{}】", logisticsOrderOperateLogEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsOrderOperateLogEntity.getId(), "物流平台订单操作记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsOrderOperateLogEntity, null, logisticsOrderOperateLogEntity.getId(), msg);
        return Boolean.TRUE;
    }

    private LogisticsOrderOperateLogEntity getByCondition(String authId, String sourceId, String businessType, String logisticsPlatform) {
        return lambdaQuery()
                .eq(LogisticsOrderOperateLogEntity::getAuthId, authId)
                .eq(LogisticsOrderOperateLogEntity::getSourceId, sourceId)
                .eq(LogisticsOrderOperateLogEntity::getBusinessType, businessType)
                .eq(LogisticsOrderOperateLogEntity::getLogisticsPlatform, logisticsPlatform)
                .last("limit 1").one();
    }

    @Override
    public LogisticsOrderOperateLogEntity addOperateLog(String authId, String sourceId, String businessType, String logisticsPlatform, String status, String requestParamJson, String responseParamJson) {
        //查询是否存在记录
        LogisticsOrderOperateLogEntity logisticsOrderOperateLogEntity = this.getByCondition(authId, sourceId, businessType, logisticsPlatform);
        if (Objects.isNull(logisticsOrderOperateLogEntity)) {
            logisticsOrderOperateLogEntity = new LogisticsOrderOperateLogEntity()
                    .setAuthId(authId)
                    .setSourceId(sourceId)
                    .setBusinessType(businessType)
                    .setLogisticsPlatform(logisticsPlatform)
                    .setStatus(status)
                    .setRequestParamJson(requestParamJson)
                    .setResponseParamJson(responseParamJson);
        } else {
            logisticsOrderOperateLogEntity.setAuthId(authId)
                    .setSourceId(sourceId)
                    .setBusinessType(businessType)
                    .setLogisticsPlatform(logisticsPlatform)
                    .setStatus(status)
                    .setRequestParamJson(requestParamJson)
                    .setResponseParamJson(responseParamJson);
        }
        this.saveOrUpdate(logisticsOrderOperateLogEntity);
        return logisticsOrderOperateLogEntity;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsOrderOperateLogEntity logisticsOrderOperateLogEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
