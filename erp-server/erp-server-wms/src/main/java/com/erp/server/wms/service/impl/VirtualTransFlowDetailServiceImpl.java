package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.VirtualTransFlowDetailEntity;
import com.erp.server.wms.mapper.VirtualTransFlowDetailMapper;
import com.erp.server.wms.service.VirtualTransFlowDetailService;
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
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 虚拟仓库存流水明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualTransFlowDetailServiceImpl extends SuperServiceImpl<VirtualTransFlowDetailMapper, VirtualTransFlowDetailEntity> implements VirtualTransFlowDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualTransFlowDetailDTO.AddDTO addDTO) {
        VirtualTransFlowDetailEntity virtualTransFlowDetailEntity = new VirtualTransFlowDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualTransFlowDetailEntity);

        // 数据处理
        handleData(virtualTransFlowDetailEntity);

        log.info("开始新增虚拟仓库存流水明细");
        boolean save = super.save(virtualTransFlowDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存流水明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓库存流水明细" , virtualTransFlowDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualTransFlowDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualTransFlowDetailEntity.getId(), virtualTransFlowDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualTransFlowDetailDTO.UpdateDTO updateDTO) {
        VirtualTransFlowDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓库存流水明细"));
        VirtualTransFlowDetailEntity virtualTransFlowDetailEntity =  BeanMapperUtils.map(VirtualTransFlowDetailEntity.class, updateDTO);

        // 数据处理
        handleData(virtualTransFlowDetailEntity);
        log.info("编辑 开始修改虚拟仓库存流水明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualTransFlowDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存流水明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录虚拟仓库存流水明细日志数据，id：【{}】", virtualTransFlowDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualTransFlowDetailEntity.getId(), "虚拟仓库存流水明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualTransFlowDetailEntity, null, virtualTransFlowDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualTransFlowDetailEntity virtualTransFlowDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
