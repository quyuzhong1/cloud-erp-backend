package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.LogisticsWarehouseDTO;
import com.erp.model.tms.entity.LogisticsWarehouseEntity;
import com.erp.server.tms.mapper.LogisticsWarehouseMapper;
import com.erp.server.tms.service.LogisticsWarehouseService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 海外仓物流商 仓库表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsWarehouseServiceImpl extends SuperServiceImpl<LogisticsWarehouseMapper, LogisticsWarehouseEntity> implements LogisticsWarehouseService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsWarehouseDTO.AddDTO addDTO) {
        LogisticsWarehouseEntity logisticsWarehouseEntity = new LogisticsWarehouseEntity();
        BeanMapperUtils.copy(addDTO, logisticsWarehouseEntity);

        // 数据处理
        handleData(logisticsWarehouseEntity);

        log.info("开始新增海外仓物流商 仓库单");
        boolean save = super.save(logisticsWarehouseEntity);
        if(!save) {
            throw new ServiceException("海外仓物流商 仓库单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "海外仓物流商 仓库单" , logisticsWarehouseEntity.getId());
        
        operateLogService.addModuleOperateLog(msg, null, logisticsWarehouseEntity.getId(), "新增操作");
        

        return new BaseResultDTO.AddDTO(logisticsWarehouseEntity.getId(), logisticsWarehouseEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsWarehouseDTO.UpdateDTO updateDTO) {
        LogisticsWarehouseEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓物流商 仓库单"));
        LogisticsWarehouseEntity logisticsWarehouseEntity =  BeanMapperUtils.map(LogisticsWarehouseEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsWarehouseEntity);
        log.info("编辑 开始修改海外仓物流商 仓库单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsWarehouseEntity);
        if(!save) {
            throw new ServiceException("海外仓物流商 仓库单保存失败");
        }
        

        // 记录主单操作日志
            log.info("编辑 开始记录海外仓物流商 仓库单日志数据，id：【{}】", logisticsWarehouseEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsWarehouseEntity.getId(), "海外仓物流商 仓库单");
        
        operateLogService.addModuleOperateLogByObj(old, logisticsWarehouseEntity, null, logisticsWarehouseEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsWarehouseEntity> listByLogisticsSupplierId(String logisticsSupplierId) {
        return this.lambdaQuery().eq(LogisticsWarehouseEntity::getMainId,logisticsSupplierId).list();
    }

    @Override
    public List<LogisticsWarehouseEntity> listByLogisticsSupplierIds(List<String> logisticsSupplierIds) {
        if (CollectionUtils.isEmpty(logisticsSupplierIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsWarehouseEntity::getMainId,logisticsSupplierIds).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsWarehouseEntity logisticsWarehouseEntity) {
    
    }
}
