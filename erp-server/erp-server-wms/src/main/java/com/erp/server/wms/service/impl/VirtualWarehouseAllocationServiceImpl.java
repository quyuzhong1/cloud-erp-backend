package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationMapper;
import com.erp.server.wms.service.VirtualWarehouseAllocationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 虚拟仓分货单 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@Service
public class VirtualWarehouseAllocationServiceImpl extends SuperServiceImpl<VirtualWarehouseAllocationMapper, VirtualWarehouseAllocationEntity> implements VirtualWarehouseAllocationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseAllocationDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = new VirtualWarehouseAllocationEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationEntity);

        // 数据处理
        handleData(virtualWarehouseAllocationEntity);

        log.info("开始新增虚拟仓分货单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        virtualWarehouseAllocationEntity.setCode(code);
        boolean save = super.save(virtualWarehouseAllocationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓分货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓分货单", virtualWarehouseAllocationEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseAllocationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓分货单"));
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = BeanMapperUtils.map(VirtualWarehouseAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseAllocationEntity);
        log.info("编辑 开始修改虚拟仓分货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualWarehouseAllocationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓分货单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓分货单日志数据，单号：【{}】", virtualWarehouseAllocationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationEntity.getCode(), "虚拟仓分货单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseAllocationEntity, null, virtualWarehouseAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return PagingVO
     * @author hyj
     * @date: 2024-06-05
     */
    @Override
    public PagingVO<VirtualWarehouseAllocationDTO.ListDTO> paging(PagingDTO<VirtualWarehouseAllocationDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualWarehouseAllocationDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        return new PagingVO(pageData);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
