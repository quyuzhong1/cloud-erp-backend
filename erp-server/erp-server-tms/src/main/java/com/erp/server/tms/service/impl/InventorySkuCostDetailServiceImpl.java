package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;
import com.erp.model.tms.entity.InventorySkuCostDetailEntity;
import com.erp.server.tms.mapper.InventorySkuCostDetailMapper;
import com.erp.server.tms.service.InventorySkuCostDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * SKU成本明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
 */
@Slf4j
@Service
public class InventorySkuCostDetailServiceImpl extends SuperServiceImpl<InventorySkuCostDetailMapper, InventorySkuCostDetailEntity> implements InventorySkuCostDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InventorySkuCostDetailDTO.AddDTO addDTO) {
        InventorySkuCostDetailEntity inventorySkuCostDetailEntity = new InventorySkuCostDetailEntity();
        BeanMapperUtils.copy(addDTO, inventorySkuCostDetailEntity);

        // 数据处理
        handleData(inventorySkuCostDetailEntity);

        log.info("开始新增SKU成本明细");
        boolean save = super.save(inventorySkuCostDetailEntity);
        if (!save) {
            throw new ServiceException("SKU成本明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "SKU成本明细", inventorySkuCostDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, inventorySkuCostDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(inventorySkuCostDetailEntity.getId(), inventorySkuCostDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InventorySkuCostDetailDTO.UpdateDTO updateDTO) {
        InventorySkuCostDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "SKU成本明细"));
        InventorySkuCostDetailEntity inventorySkuCostDetailEntity = BeanMapperUtils.map(InventorySkuCostDetailEntity.class, updateDTO);

        // 数据处理
        handleData(inventorySkuCostDetailEntity);
        log.info("编辑 开始修改SKU成本明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(inventorySkuCostDetailEntity);
        if (!save) {
            throw new ServiceException("SKU成本明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录SKU成本明细日志数据，id：【{}】", inventorySkuCostDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), inventorySkuCostDetailEntity.getId(), "SKU成本明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, inventorySkuCostDetailEntity, null, inventorySkuCostDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void removeByMainId(String id) {
        if (!StrUtil.isBlank(id)) {
            this.lambdaUpdate().eq(InventorySkuCostDetailEntity::getMainId, id).remove();
        }
    }

    @Override
    public List<InventorySkuCostDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(InventorySkuCostDetailEntity::getMainId, mainIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buildDetail(List<InventorySkuCostDetailEntity> detailEntityList, String id) {
        if (CollectionUtils.isEmpty(detailEntityList)) {
            //明细为空则清空
            lambdaUpdate().eq(InventorySkuCostDetailEntity::getMainId, id).remove();
            return;
        }
        List<String> newDetailIds = detailEntityList.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getId())).map(InventorySkuCostDetailEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newDetailIds)) {
            //明细为空则清空
            lambdaUpdate().eq(InventorySkuCostDetailEntity::getMainId, id).remove();
        }
        List<InventorySkuCostDetailEntity> oldDetailEntityList = this.listByMainIds(Collections.singletonList(id));
        if (!CollectionUtils.isEmpty(oldDetailEntityList)) {
            List<String> oldDetailIds = oldDetailEntityList.stream().map(InventorySkuCostDetailEntity::getId).distinct().collect(Collectors.toList());
            List<String> notExistDetailIds = oldDetailIds.stream().filter(e -> !newDetailIds.contains(e)).distinct().collect(Collectors.toList());
            //清空不存在的明细记录
            this.removeByIds(notExistDetailIds);
        }
        detailEntityList.forEach(inventorySkuCostDetailEntity -> {
            inventorySkuCostDetailEntity.setMainId(id);
        });
        this.saveOrUpdateBatch(detailEntityList);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(InventorySkuCostDetailEntity inventorySkuCostDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
