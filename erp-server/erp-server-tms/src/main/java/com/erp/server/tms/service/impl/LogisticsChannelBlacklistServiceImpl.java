package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsChannelBlacklistEntity;
import com.erp.server.tms.mapper.LogisticsChannelBlacklistMapper;
import com.erp.server.tms.service.LogisticsChannelBlacklistService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsChannelBlacklistDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 渠道黑名单表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsChannelBlacklistServiceImpl extends SuperServiceImpl<LogisticsChannelBlacklistMapper, LogisticsChannelBlacklistEntity> implements LogisticsChannelBlacklistService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsChannelBlacklistDTO.AddDTO addDTO) {
        LogisticsChannelBlacklistEntity logisticsChannelBlacklistEntity = new LogisticsChannelBlacklistEntity();
        BeanMapperUtils.copy(addDTO, logisticsChannelBlacklistEntity);

        // 数据处理
        handleData(logisticsChannelBlacklistEntity);

        log.info("开始新增渠道黑名单表");
        boolean save = super.save(logisticsChannelBlacklistEntity);
        if(!save) {
            throw new ServiceException("渠道黑名单表保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "渠道黑名单表" , logisticsChannelBlacklistEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsChannelBlacklistEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsChannelBlacklistEntity.getId(), logisticsChannelBlacklistEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsChannelBlacklistDTO.UpdateDTO updateDTO) {
        LogisticsChannelBlacklistEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "渠道黑名单表"));
        LogisticsChannelBlacklistEntity logisticsChannelBlacklistEntity =  BeanMapperUtils.map(LogisticsChannelBlacklistEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsChannelBlacklistEntity);
        log.info("编辑 开始修改渠道黑名单表数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsChannelBlacklistEntity);
        if(!save) {
            throw new ServiceException("渠道黑名单表保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录渠道黑名单表日志数据，id：【{}】", logisticsChannelBlacklistEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsChannelBlacklistEntity.getId(), "渠道黑名单表");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsChannelBlacklistEntity, null, logisticsChannelBlacklistEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsChannelBlacklistEntity logisticsChannelBlacklistEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
