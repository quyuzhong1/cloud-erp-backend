package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.entity.VirtualAdjustDetailEntity;
import com.erp.model.wms.entity.VirtualAdjustEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryInOutEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.VirtualAdjustDetailMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

import static cn.hutool.json.XMLTokener.entity;

/**
 * <p>
 * 虚拟仓调整单明细表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-06-09
 */
@Slf4j
@Service
public class VirtualAdjustDetailServiceImpl extends SuperServiceImpl<VirtualAdjustDetailMapper, VirtualAdjustDetailEntity> implements VirtualAdjustDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualAdjustDetailDTO.AddDTO addDTO) {
        VirtualAdjustDetailEntity virtualAdjustDetailEntity = new VirtualAdjustDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualAdjustDetailEntity);

        // 数据处理
        handleData(virtualAdjustDetailEntity);

        log.info("开始新增虚拟仓调整单明细单");
        boolean save = super.save(virtualAdjustDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓调整单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓调整单明细单" , virtualAdjustDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), virtualAdjustDetailEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(virtualAdjustDetailEntity.getId(), virtualAdjustDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualAdjustDetailDTO.UpdateDTO addOrUpdateDTO) {
        VirtualAdjustDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓调整单明细单"));
        VirtualAdjustDetailEntity virtualAdjustDetailEntity =  BeanMapperUtils.map(VirtualAdjustDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(virtualAdjustDetailEntity);
        log.info("编辑 开始修改虚拟仓调整单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualAdjustDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓调整单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓调整单明细单日志数据，id：【{}】", virtualAdjustDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualAdjustDetailEntity.getId(), "虚拟仓调整单明细单");
        operateLogService.addModuleOperateLogByObj(old, virtualAdjustDetailEntity, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), virtualAdjustDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(String mainId, List<VirtualAdjustDetailEntity> detailEntityList) {
        if (CollUtil.isEmpty(detailEntityList)) {
            return;
        }
        handleBatchData(detailEntityList, mainId);
        List<String> detailIds = detailEntityList.stream().map(VirtualAdjustDetailEntity::getId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<VirtualAdjustDetailEntity> detailEntityList1 = this.listByMainIdList(Collections.singletonList(mainId));
        detailEntityList1.stream().filter(e -> !detailIds.contains(e.getId())).forEach(e -> {
            //删除原数据
            log.info("删除原虚拟仓调整单明细单数据，id：【{}】", e.getId());
            this.removeById(e.getId());
            String msg = StrUtil.format("删除一行明细SKU【{}】 ",  e.getSkuNo());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), mainId, "删除虚拟仓调整单明细数据");
        });
        //批量修改
        log.info("批量修改虚拟仓调整单明细单数据，mainId：【{}】", mainId);
        List<VirtualAdjustDetailEntity> addList = detailEntityList.stream().filter(detail -> CharSequenceUtil.isBlank(detail.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(addList)){
            this.saveBatch(addList);
            String msg = StrUtil.format("新增一行明细SKU【{}】", addList.stream().map(VirtualAdjustDetailEntity::getSkuNo).collect(Collectors.joining(",")), "虚拟仓调整单明细单");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), mainId, "新增虚拟仓调整单明细数据");
        }
        List<VirtualAdjustDetailEntity> updateList = detailEntityList.stream().filter(detail -> CharSequenceUtil.isNotBlank(detail.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(updateList)){
            updateList.forEach(detailEntity -> {
                VirtualAdjustDetailEntity old = detailEntityList1.stream().filter(e -> e.getId().equals(detailEntity.getId())).findFirst().orElse(null);
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), detailEntity.getMainId(), "", "编辑操作");
                this.updateById(detailEntity);
            });
        }
    }

    @Override
    public List<VirtualAdjustDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollUtil.isNotEmpty(mainIdList)){
            return super.list(new LambdaQueryWrapper<VirtualAdjustDetailEntity>().in(VirtualAdjustDetailEntity::getMainId, mainIdList));
        }
        return Collections.emptyList();
    }

    @Override
    public void removeByMainId(String id) {
        if (CharSequenceUtil.isNotBlank(id)){
            super.remove(new LambdaQueryWrapper<VirtualAdjustDetailEntity>().eq(VirtualAdjustDetailEntity::getMainId, id));
        }
    }

    private void handleBatchData(List<VirtualAdjustDetailEntity> detailEntityList, String mainId) {
        detailEntityList.forEach(e -> e.setMainId(mainId));
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualAdjustDetailEntity virtualAdjustDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
