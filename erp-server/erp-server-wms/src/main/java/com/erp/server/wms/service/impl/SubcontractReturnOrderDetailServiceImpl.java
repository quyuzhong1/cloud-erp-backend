package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SubcontractReturnOrderDetailEntity;
import com.erp.server.wms.mapper.SubcontractReturnOrderDetailMapper;
import com.erp.server.wms.service.SubcontractReturnOrderDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SubcontractReturnOrderDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 委外退料明细单 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
@Slf4j
@Service
public class SubcontractReturnOrderDetailServiceImpl extends SuperServiceImpl<SubcontractReturnOrderDetailMapper, SubcontractReturnOrderDetailEntity> implements SubcontractReturnOrderDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SubcontractReturnOrderDetailDTO.AddDTO addDTO) {
        SubcontractReturnOrderDetailEntity subcontractReturnOrderDetailEntity = new SubcontractReturnOrderDetailEntity();
        BeanMapperUtils.copy(addDTO, subcontractReturnOrderDetailEntity);

        // 数据处理
        handleData(subcontractReturnOrderDetailEntity);

        log.info("开始新增委外退料明细单");
        boolean save = super.save(subcontractReturnOrderDetailEntity);
        if(!save) {
            throw new ServiceException("委外退料明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "委外退料明细单" , subcontractReturnOrderDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, subcontractReturnOrderDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(subcontractReturnOrderDetailEntity.getId(), subcontractReturnOrderDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SubcontractReturnOrderDetailDTO.UpdateDTO updateDTO) {
        SubcontractReturnOrderDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "委外退料明细单"));
        SubcontractReturnOrderDetailEntity subcontractReturnOrderDetailEntity =  BeanMapperUtils.map(SubcontractReturnOrderDetailEntity.class, updateDTO);

        // 数据处理
        handleData(subcontractReturnOrderDetailEntity);
        log.info("编辑 开始修改委外退料明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(subcontractReturnOrderDetailEntity);
        if(!save) {
            throw new ServiceException("委外退料明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录委外退料明细单日志数据，id：【{}】", subcontractReturnOrderDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), subcontractReturnOrderDetailEntity.getId(), "委外退料明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, subcontractReturnOrderDetailEntity, null, subcontractReturnOrderDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SubcontractReturnOrderDetailEntity subcontractReturnOrderDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
