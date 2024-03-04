package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsServicePlatformEntity;
import com.erp.server.tms.mapper.LogisticsServicePlatformMapper;
import com.erp.server.tms.service.LogisticsServicePlatformService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsServicePlatformDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流平台服务表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-04
 */
@Slf4j
@Service
public class LogisticsServicePlatformServiceImpl extends SuperServiceImpl<LogisticsServicePlatformMapper, LogisticsServicePlatformEntity> implements LogisticsServicePlatformService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsServicePlatformDTO.AddDTO addDTO) {
        LogisticsServicePlatformEntity logisticsServicePlatformEntity = new LogisticsServicePlatformEntity();
        BeanMapperUtils.copy(addDTO, logisticsServicePlatformEntity);

        // 数据处理
        handleData(logisticsServicePlatformEntity);

        log.info("开始新增物流平台服务单");
        boolean save = super.save(logisticsServicePlatformEntity);
        if(!save) {
            throw new ServiceException("物流平台服务单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流平台服务单" , logisticsServicePlatformEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsServicePlatformEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsServicePlatformEntity.getId(), logisticsServicePlatformEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsServicePlatformDTO.UpdateDTO updateDTO) {
        LogisticsServicePlatformEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流平台服务单"));
        LogisticsServicePlatformEntity logisticsServicePlatformEntity =  BeanMapperUtils.map(LogisticsServicePlatformEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsServicePlatformEntity);
        log.info("编辑 开始修改物流平台服务单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsServicePlatformEntity);
        if(!save) {
            throw new ServiceException("物流平台服务单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流平台服务单日志数据，id：【{}】", logisticsServicePlatformEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsServicePlatformEntity.getId(), "物流平台服务单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsServicePlatformEntity, null, logisticsServicePlatformEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsServicePlatformEntity logisticsServicePlatformEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
