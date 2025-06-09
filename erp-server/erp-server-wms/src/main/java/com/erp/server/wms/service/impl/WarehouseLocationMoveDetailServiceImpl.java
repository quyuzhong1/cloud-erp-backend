package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.entity.WarehouseLocationMoveEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.WarehouseLocationMoveDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(WarehouseLocationMoveDTO.AddDTO addDTO, WarehouseLocationMoveEntity warehouseLocationMoveEntity) {
        List<WarehouseLocationMoveDetailEntity> warehouseLocationMoveDetailEntities = BeanMapperUtils.copyList(WarehouseLocationMoveDetailEntity.class, addDTO.getDetailList());

        // 数据处理
        if (!addDTO.getPcShow()) {
            handleData(warehouseLocationMoveDetailEntities, warehouseLocationMoveEntity, addDTO.getWarehouseId());
        }else{
            pcHandleData(warehouseLocationMoveDetailEntities, warehouseLocationMoveEntity);
        }
        log.info("开始新增仓位移动明细单");
        boolean save = super.saveBatch(warehouseLocationMoveDetailEntities);
        if(!save) {
            throw new ServiceException(ApiError.LOCATION_MOVE_DETAIL_ADD);
        }
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WarehouseLocationMoveDTO.UpdateDTO dto, WarehouseLocationMoveEntity warehouseLocationMoveEntity) {
        if (CollectionUtils.isEmpty(dto.getDetailList())) {
            throw new ServiceException(ApiError.ERROR_1040, SourceTypeEnum.SO_B2C.getName());
        }

        //原明细数据
        List<WarehouseLocationMoveDetailEntity> oldList = this.listByMainIds(Collections.singletonList(warehouseLocationMoveEntity.getId()));
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<WarehouseLocationMoveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        List<WarehouseLocationMoveDetailEntity> list = BeanMapperUtils.copyList(WarehouseLocationMoveDetailEntity.class, dto.getDetailList());
        // 数据处理
        if (!dto.getPcShow()) {
            handleData(list, warehouseLocationMoveEntity, dto.getWarehouseId());
        }else{
            pcHandleData(list, warehouseLocationMoveEntity);
        }
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
    private void handleData(List<WarehouseLocationMoveDetailEntity> list, WarehouseLocationMoveEntity warehouseLocationMoveEntity, String warehouseId) {
        //获取仓库信息
        WarehouseEntity warehouseEntity = Optional.ofNullable(warehouseService.getById(warehouseId)).orElse(new WarehouseEntity());
        for (WarehouseLocationMoveDetailEntity detailEntity : list) {
            InventoryDTO.PdaSearchParamDTO paramDTO = new InventoryDTO.PdaSearchParamDTO();
            paramDTO.setOrgId(warehouseEntity.getOrgId());
            paramDTO.setWarehouseId(warehouseId);
            paramDTO.setSkuIds(Collections.singletonList(detailEntity.getSkuId()));
            if ((ObjectUtil.isEmpty(detailEntity.getInWarehouseLocation()) && ObjectUtil.isEmpty(detailEntity.getOutWarehouseLocation()))
                    || detailEntity.getInWarehouseLocation().equals(detailEntity.getOutWarehouseLocation())) {
                throw new ServiceException(ApiError.ERROR_CANNOT_SAME_POSITION);
            }
            paramDTO.setWarehouseLocations(Collections.singletonList(detailEntity.getOutWarehouseLocation()));
            List<InventoryDTO.PdaInventoryDTO> inventoryByParams = inventoryService.getInventoryByParam(paramDTO);
            InventoryDTO.PdaInventoryDTO inventoryByParam = inventoryByParams.stream().filter(req -> req.getWarehouseId().equals(warehouseId)
                    && req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);

            if (SourceTypeEnum.PICKING_LISTS_SUBTRACT.getCode().equals(warehouseLocationMoveEntity.getSourceType())) {
                if (ObjectUtil.isEmpty(inventoryByParam) || detailEntity.getQty() > inventoryByParam.getFrozenQty()) {
                    throw new ServiceException(ApiError.LOCATION_MOVE_FROZEN_QTY_ERROR, detailEntity.getSkuNo());
                }
            }else {
                if (InventoryStatusEnum.USABLE.getCode().equals(detailEntity.getOutInventoryStatus())) {
                    if (ObjectUtil.isEmpty(inventoryByParam) || detailEntity.getQty() > inventoryByParam.getUsableQty()) {
                        throw new ServiceException(ApiError.LOCATION_MOVE_QTY_ERROR, detailEntity.getSkuNo());
                    }
                }
                if (InventoryStatusEnum.FROZEN.getCode().equals(detailEntity.getOutInventoryStatus())) {
                    if (ObjectUtil.isEmpty(inventoryByParam) || detailEntity.getQty() > inventoryByParam.getFrozenQty()) {
                        throw new ServiceException(ApiError.LOCATION_MOVE_FROZEN_QTY_ERROR, detailEntity.getSkuNo());
                    }
                }
            }
            detailEntity.setInInventoryStatus(Optional.ofNullable(detailEntity.getInInventoryStatus()).orElse(InventoryStatusEnum.USABLE.getCode()));
            detailEntity.setOutInventoryStatus(Optional.ofNullable(detailEntity.getOutInventoryStatus()).orElse(InventoryStatusEnum.USABLE.getCode()));
            detailEntity.setMainId(warehouseLocationMoveEntity.getId());
            if (CharSequenceUtil.isNotBlank(warehouseId)){
                detailEntity.setWarehouseId(warehouseId);
                detailEntity.setWarehouseName(warehouseEntity.getName());
            }
        }

        //添加操作日志
        List<String> addList = list.stream().map(WarehouseLocationMoveDetailEntity::getId).filter(StringUtils::isBlank).collect(Collectors.toList());
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<WarehouseLocationMoveDetailEntity> receiveDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = receiveDetailEntityList.stream().map(obj -> new Pair<>(warehouseLocationMoveEntity.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), addPairList, "编辑操作");
        }
    }
    /**
    * 新增修改处理数据
    */
    private void pcHandleData(List<WarehouseLocationMoveDetailEntity> list, WarehouseLocationMoveEntity warehouseLocationMoveEntity) {
        //获取仓库信息
        for (WarehouseLocationMoveDetailEntity detailEntity : list) {
            String warehouseId = detailEntity.getWarehouseId();
            WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
            InventoryDTO.PdaSearchParamDTO paramDTO = new InventoryDTO.PdaSearchParamDTO();
            paramDTO.setOrgId(warehouseEntity.getOrgId());
            paramDTO.setWarehouseId(warehouseId);
            paramDTO.setSkuIds(Collections.singletonList(detailEntity.getSkuId()));
            detailEntity.setInInventoryStatus(Optional.ofNullable(detailEntity.getInInventoryStatus()).orElse(InventoryStatusEnum.USABLE.getCode()));
            detailEntity.setOutInventoryStatus(Optional.ofNullable(detailEntity.getOutInventoryStatus()).orElse(InventoryStatusEnum.USABLE.getCode()));
            if ((ObjectUtil.isEmpty(detailEntity.getInWarehouseLocation()) && ObjectUtil.isEmpty(detailEntity.getOutWarehouseLocation()))
                    || detailEntity.getInWarehouseLocation().equals(detailEntity.getOutWarehouseLocation())) {
                throw new ServiceException(ApiError.ERROR_CANNOT_SAME_POSITION);
            }
            paramDTO.setWarehouseLocations(Collections.singletonList(detailEntity.getOutWarehouseLocation()));
            List<InventoryDTO.PdaInventoryDTO> inventoryByParams = inventoryService.getInventoryByParam(paramDTO);
            InventoryDTO.PdaInventoryDTO inventoryByParam = inventoryByParams.stream().filter(req -> req.getWarehouseId().equals(warehouseId)
                    && req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (SourceTypeEnum.PICKING_LISTS_SUBTRACT.getCode().equals(warehouseLocationMoveEntity.getSourceType())) {
                if (ObjectUtil.isEmpty(inventoryByParam) || detailEntity.getQty() > inventoryByParam.getFrozenQty()) {
                    throw new ServiceException(ApiError.LOCATION_MOVE_FROZEN_QTY_ERROR, detailEntity.getSkuNo());
                }
            }else {
                if (InventoryStatusEnum.USABLE.getCode().equals(detailEntity.getOutInventoryStatus())) {
                    if (ObjectUtil.isEmpty(inventoryByParam) || detailEntity.getQty() > inventoryByParam.getUsableQty()) {
                        throw new ServiceException(ApiError.LOCATION_MOVE_QTY_ERROR, detailEntity.getSkuNo());
                    }
                }
                if (InventoryStatusEnum.FROZEN.getCode().equals(detailEntity.getOutInventoryStatus())) {
                    if (ObjectUtil.isEmpty(inventoryByParam) || detailEntity.getQty() > inventoryByParam.getFrozenQty()) {
                        throw new ServiceException(ApiError.LOCATION_MOVE_FROZEN_QTY_ERROR, detailEntity.getSkuNo());
                    }
                }
            }
            detailEntity.setMainId(warehouseLocationMoveEntity.getId());
            List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Collections.singletonList(detailEntity.getWarehouseId()));
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(req -> req.getId().equals(detailEntity.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            detailEntity.setWarehouseName(updateDTO.getName());
            detailEntity.setInventoryOrgId(updateDTO.getOrgId());
            //获取核算公司
            SysAccountingCompanyEntity companyEntity = sysUserFeign.getCompanyById(updateDTO.getOrgId());
            if (ObjectUtil.isNotEmpty(companyEntity)) {
                detailEntity.setInventoryOrgName(companyEntity.getCompanyName());
            }
        }

        //添加操作日志
        List<String> addList = list.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(WarehouseLocationMoveDetailEntity::getId).collect(Collectors.toList());
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<WarehouseLocationMoveDetailEntity> receiveDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = receiveDetailEntityList.stream().map(obj -> new Pair<>(warehouseLocationMoveEntity.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<WarehouseLocationMoveDetailDTO.UpdateDTO> newList, List<WarehouseLocationMoveDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(WarehouseLocationMoveDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(WarehouseLocationMoveDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
