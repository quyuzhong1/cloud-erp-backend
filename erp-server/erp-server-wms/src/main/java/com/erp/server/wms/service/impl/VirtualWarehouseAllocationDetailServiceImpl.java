package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationDetailMapper;
import com.erp.server.wms.service.VirtualWarehouseAllocationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 虚拟仓分货单明细 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@Service
public class VirtualWarehouseAllocationDetailServiceImpl extends SuperServiceImpl<VirtualWarehouseAllocationDetailMapper, VirtualWarehouseAllocationDetailEntity> implements VirtualWarehouseAllocationDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO batchAdd(VirtualWarehouseAllocationDetailDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity = new VirtualWarehouseAllocationDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationDetailEntity);

        // 数据处理
        handleData(virtualWarehouseAllocationDetailEntity);

        log.info("开始新增虚拟仓分货单明细");
        boolean save = super.save(virtualWarehouseAllocationDetailEntity);
        if (!save) {
            throw new ServiceException("虚拟仓分货单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓分货单明细", virtualWarehouseAllocationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseAllocationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationDetailEntity.getId(), virtualWarehouseAllocationDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchUpdate(VirtualWarehouseAllocationDetailDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓分货单明细"));
        VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity = BeanMapperUtils.map(VirtualWarehouseAllocationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseAllocationDetailEntity);
        log.info("编辑 开始修改虚拟仓分货单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseAllocationDetailEntity);
        if (!save) {
            throw new ServiceException("虚拟仓分货单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓分货单明细日志数据，id：【{}】", virtualWarehouseAllocationDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationDetailEntity.getId(), "虚拟仓分货单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseAllocationDetailEntity, null, virtualWarehouseAllocationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(VirtualWarehouseAllocationDTO.AddDTO addDTO, String mainId) {
        List<VirtualWarehouseAllocationDetailEntity> detailEntityList = BeanMapperUtils.copyList(VirtualWarehouseAllocationDetailEntity.class, addDTO.getDetailList());
        handleData(detailEntityList, mainId);
        boolean save = super.saveBatch(detailEntityList);
        if (!save) {
            throw new ServiceException(ApiError.ERROR_VMALLOCATION_DETAIL_ADD);
        }
        //添加操作日志
        List<String> addList = detailEntityList.stream().map(VirtualWarehouseAllocationDetailEntity::getId).filter(StringUtils::isBlank).collect(Collectors.toList());
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<VirtualWarehouseAllocationDetailEntity> receiveDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = receiveDetailEntityList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个分货单明细【%s】", ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), addPairList, "新增操作");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdate(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO, String mainId) {
        if (CollectionUtils.isEmpty(updateDTO.getDetailList())) {
            throw new ServiceException(ApiError.ERROR_1040, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getName());
        }
        List<VirtualWarehouseAllocationDetailEntity> oldList = this.listByIds(Collections.singletonList(mainId));
        List<String> deleteIds = getDeleteIds(updateDTO.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<VirtualWarehouseAllocationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个分货单明细【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }

        List<VirtualWarehouseAllocationDetailEntity> list = BeanMapperUtils.copyList(VirtualWarehouseAllocationDetailEntity.class, updateDTO.getDetailList());
        // 数据处理
        handleData(list, mainId);
        //添加操作日志
        List<String> addList = list.stream().map(VirtualWarehouseAllocationDetailEntity::getId).filter(StringUtils::isBlank).collect(Collectors.toList());
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<VirtualWarehouseAllocationDetailEntity> receiveDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = receiveDetailEntityList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个虚拟仓分货单【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
    }

    private void handleData(List<VirtualWarehouseAllocationDetailEntity> detailEntityList, String mainId) {

    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<VirtualWarehouseAllocationDTO.DetailDto> newList, List<VirtualWarehouseAllocationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(VirtualWarehouseAllocationDTO.DetailDto::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(VirtualWarehouseAllocationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
