package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.server.wms.mapper.WarehouseLocationMoveDetailMapper;
import com.erp.server.wms.service.WarehouseLocationMoveDetailService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 仓位移动明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
 */
@Slf4j
@Service
public class WarehouseLocationMoveDetailServiceImpl extends SuperServiceImpl<WarehouseLocationMoveDetailMapper, WarehouseLocationMoveDetailEntity> implements WarehouseLocationMoveDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(List<WarehouseLocationMoveDetailDTO.AddDTO> addDTO, String mainId) {
        List<WarehouseLocationMoveDetailEntity> warehouseLocationMoveDetailEntities = BeanMapperUtils.copyList(WarehouseLocationMoveDetailEntity.class, addDTO);

        // 数据处理
        handleData(warehouseLocationMoveDetailEntities, mainId);

        log.info("开始新增仓位移动明细单");
        boolean save = super.saveBatch(warehouseLocationMoveDetailEntities);
        if(!save) {
            throw new ServiceException("仓位移动明细单保存失败");
        }

    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<WarehouseLocationMoveDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_1040, SourceTypeEnum.SO_B2C.getName());
        }

        //原明细数据
        List<WarehouseLocationMoveDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<WarehouseLocationMoveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        List<WarehouseLocationMoveDetailEntity> list = BeanMapperUtils.copyList(WarehouseLocationMoveDetailEntity.class, detailList);
        // 数据处理
        handleData(list, mainId);
        return this.saveOrUpdateBatch(list);
    }

    @Override
    public List<WarehouseLocationMoveDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(WarehouseLocationMoveDetailEntity::getMainId, mainIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeByMainId(String mainId) {
        lambdaUpdate().eq(WarehouseLocationMoveDetailEntity::getMainId, mainId).remove();
        return Boolean.TRUE;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<WarehouseLocationMoveDetailEntity> list, String mainId) {
        for (WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity : list) {

        }

        //添加操作日志
        List<String> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).map(WarehouseLocationMoveDetailEntity::getId).collect(Collectors.toList());
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<WarehouseLocationMoveDetailEntity> receiveDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = receiveDetailEntityList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<WarehouseLocationMoveDetailDTO.UpdateDTO> newList, List<WarehouseLocationMoveDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(WarehouseLocationMoveDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(WarehouseLocationMoveDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
