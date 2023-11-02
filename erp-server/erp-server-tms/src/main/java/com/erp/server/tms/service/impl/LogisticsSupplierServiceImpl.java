package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.server.tms.mapper.LogisticsSupplierMapper;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物理商表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsSupplierServiceImpl extends SuperServiceImpl<LogisticsSupplierMapper, LogisticsSupplierEntity> implements LogisticsSupplierService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsSupplierDTO.AddDTO addDTO) {
        LogisticsSupplierEntity logisticsSupplierEntity = new LogisticsSupplierEntity();
        BeanMapperUtils.copy(addDTO, logisticsSupplierEntity);

        // 数据处理
        handleData(logisticsSupplierEntity);

        log.info("开始新增物理商单");
        boolean save = super.save(logisticsSupplierEntity);
        if(!save) {
            throw new ServiceException("物理商单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物理商单" , logisticsSupplierEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsSupplierEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(fbaDeliveryEntity.getId(), fbaDeliveryEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsSupplierDTO.UpdateDTO updateDTO) {
        LogisticsSupplierEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物理商单"));
        LogisticsSupplierEntity logisticsSupplierEntity =  BeanMapperUtils.map(LogisticsSupplierEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsSupplierEntity);
        log.info("编辑 开始修改物理商单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsSupplierEntity);
        if(!save) {
            throw new ServiceException("物理商单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物理商单日志数据，id：【{}】", logisticsSupplierEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsSupplierEntity.getId(), "物理商单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsSupplierEntity, null, logisticsSupplierEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsSupplierEntity logisticsSupplierEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
