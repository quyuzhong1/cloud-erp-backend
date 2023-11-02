package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsPrintTypeEntity;
import com.erp.server.tms.mapper.LogisticsPrintTypeMapper;
import com.erp.server.tms.service.LogisticsPrintTypeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 面板打印设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsPrintTypeServiceImpl extends SuperServiceImpl<LogisticsPrintTypeMapper, LogisticsPrintTypeEntity> implements LogisticsPrintTypeService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsPrintTypeDTO.AddDTO addDTO) {
        LogisticsPrintTypeEntity logisticsPrintTypeEntity = new LogisticsPrintTypeEntity();
        BeanMapperUtils.copy(addDTO, logisticsPrintTypeEntity);

        // 数据处理
        handleData(logisticsPrintTypeEntity);

        log.info("开始新增面板打印设置单");
        boolean save = super.save(logisticsPrintTypeEntity);
        if(!save) {
            throw new ServiceException("面板打印设置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "面板打印设置单" , logisticsPrintTypeEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsPrintTypeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsPrintTypeEntity.getId(), logisticsPrintTypeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsPrintTypeDTO.UpdateDTO updateDTO) {
        LogisticsPrintTypeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "面板打印设置单"));
        LogisticsPrintTypeEntity logisticsPrintTypeEntity =  BeanMapperUtils.map(LogisticsPrintTypeEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsPrintTypeEntity);
        log.info("编辑 开始修改面板打印设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsPrintTypeEntity);
        if(!save) {
            throw new ServiceException("面板打印设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录面板打印设置单日志数据，id：【{}】", logisticsPrintTypeEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsPrintTypeEntity.getId(), "面板打印设置单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsPrintTypeEntity, null, logisticsPrintTypeEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsPrintTypeEntity logisticsPrintTypeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
