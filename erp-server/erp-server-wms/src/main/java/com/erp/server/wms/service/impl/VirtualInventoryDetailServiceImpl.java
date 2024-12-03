package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import com.erp.server.wms.mapper.VirtualInventoryDetailMapper;
import com.erp.server.wms.service.VirtualInventoryDetailService;
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
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 虚拟仓库明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualInventoryDetailServiceImpl extends SuperServiceImpl<VirtualInventoryDetailMapper, VirtualInventoryDetailEntity> implements VirtualInventoryDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualInventoryDetailDTO.AddDTO addDTO) {
        VirtualInventoryDetailEntity virtualInventoryDetailEntity = new VirtualInventoryDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualInventoryDetailEntity);

        // 数据处理
        handleData(virtualInventoryDetailEntity);

        log.info("开始新增虚拟仓库明细");
        boolean save = super.save(virtualInventoryDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓库明细" , virtualInventoryDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualInventoryDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualInventoryDetailEntity.getId(), virtualInventoryDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualInventoryDetailDTO.UpdateDTO updateDTO) {
        VirtualInventoryDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓库明细"));
        VirtualInventoryDetailEntity virtualInventoryDetailEntity =  BeanMapperUtils.map(VirtualInventoryDetailEntity.class, updateDTO);

        // 数据处理
        handleData(virtualInventoryDetailEntity);
        log.info("编辑 开始修改虚拟仓库明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualInventoryDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录虚拟仓库明细日志数据，id：【{}】", virtualInventoryDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualInventoryDetailEntity.getId(), "虚拟仓库明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualInventoryDetailEntity, null, virtualInventoryDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryDetailEntity virtualInventoryDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
