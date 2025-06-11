package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.vo.SkuVO;
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
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Lazy
    @Resource
    private VirtualWarehouseService virtualWarehouseService;
    @Resource
    private VirtualInventoryService virtualInventoryService;
    @Resource
    private InventoryService inventoryService;
    @GlobalTransactional(rollbackFor = Exception.class)
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
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualAdjustDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

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
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualAdjustDetailEntity, null, virtualAdjustDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void addDetail(String mainId, List<VirtualAdjustDetailDTO.AddDTO> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        List<VirtualAdjustDetailEntity> detailEntityList = BeanMapperUtils.copyList(VirtualAdjustDetailEntity.class, detailList);
        handleBatchData(detailEntityList, mainId);
        //批量新增
        log.info("批量新增虚拟仓调整单明细单数据，mainId：【{}】", mainId);
        boolean saveBatch = super.saveBatch(detailEntityList);
        if(!saveBatch) {
            throw new ServiceException("虚拟仓调整单明细单保存失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(String mainId, List<VirtualAdjustDetailDTO.UpdateDTO> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        List<VirtualAdjustDetailEntity> detailEntityList = BeanMapperUtils.copyList(VirtualAdjustDetailEntity.class, detailList);
        handleBatchData(detailEntityList, mainId);
        List<String> detailIds = detailEntityList.stream().map(VirtualAdjustDetailEntity::getId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        //删除原数据
        log.info("删除原虚拟仓调整单明细单数据，mainId：【{}】", mainId);
        super.remove(new LambdaQueryWrapper<VirtualAdjustDetailEntity>().eq(VirtualAdjustDetailEntity::getMainId, mainId).notIn(CollUtil.isNotEmpty(detailIds),VirtualAdjustDetailEntity::getId,detailIds));
        //批量修改
        log.info("批量修改虚拟仓调整单明细单数据，mainId：【{}】", mainId);
        List<VirtualAdjustDetailEntity> addList = detailEntityList.stream().filter(detail -> CharSequenceUtil.isBlank(detail.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(addList)){
            this.saveBatch(addList);
        }
        List<VirtualAdjustDetailEntity> updateList = detailEntityList.stream().filter(detail -> CharSequenceUtil.isNotBlank(detail.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
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

    @Override
    public void validateSubmit(VirtualAdjustEntity entity) {
        //校验库存
        checkInventory(entity.getId(),listByMainIdList(Collections.singletonList(entity.getId())));
    }

    private void handleBatchData(List<VirtualAdjustDetailEntity> detailEntityList, String mainId) {
        //补充sku信息
        List<String> skuIdList = detailEntityList.stream().map(VirtualAdjustDetailEntity::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIdList);
        //补充仓库信息
        List<String> virtualWarehouseIdList = detailEntityList.stream().map(VirtualAdjustDetailEntity::getVirtualWarehouseId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<VirtualWarehouseDTO.ViewWarehouseDTO> warehouseDTOS = virtualWarehouseService.listWarehouseInfoByIds(virtualWarehouseIdList);
        detailEntityList.forEach(detail -> {
            detail.setMainId(mainId);
            if (detail.getQty() > 0) {
                detail.setType(InventoryInOutEnum.IN_STOCK.getCode());
            }else {
                detail.setType(InventoryInOutEnum.OUT_STOCK.getCode());
            }
            if (CharSequenceUtil.isNotBlank(detail.getSkuId())){
                SkuVO skuVO = skuVOS.stream().filter(sku -> sku.getSkuId().equals(detail.getSkuId())).findFirst().orElse(null);
                detail.setSkuNo(Objects.nonNull(skuVO) ? skuVO.getSkuNo() : detail.getSkuNo());
                detail.setProductName(Objects.nonNull(skuVO)? skuVO.getSkuName() : detail.getProductName());
            }
            if (CharSequenceUtil.isNotBlank(detail.getVirtualWarehouseId())){
                VirtualWarehouseDTO.ViewWarehouseDTO warehouseDTO = warehouseDTOS.stream().filter(warehouse -> warehouse.getVirtualWarehouseId().equals(detail.getVirtualWarehouseId())).findFirst().orElse(null);
                detail.setWarehouseId(Objects.nonNull(warehouseDTO)? warehouseDTO.getWarehouseId() : detail.getWarehouseId());
                detail.setWarehouseName(Objects.nonNull(warehouseDTO)? warehouseDTO.getWarehouseName() : detail.getWarehouseName());
                detail.setVirtualWarehouseName(Objects.nonNull(warehouseDTO)? warehouseDTO.getVirtualWarehouseName() : detail.getVirtualWarehouseName());
            }
            if (CharSequenceUtil.isBlank(detail.getSkuId())){
                throw new ServiceException("SKU不能为空");
            }
            if (CharSequenceUtil.isBlank(detail.getVirtualWarehouseId())){
                throw new ServiceException("虚拟仓不能为空");
            }
            if (CharSequenceUtil.isBlank(detail.getWarehouseId())){
                throw new ServiceException("实体仓不能为空");
            }
        });
        //校验库存
        checkInventory(mainId,detailEntityList);
    }

    private void checkInventory(String mainId, List<VirtualAdjustDetailEntity> detailList) {
        List<String> skuIds = detailList.stream().map(VirtualAdjustDetailEntity::getSkuId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<String> warehouseIds = detailList.stream().map(VirtualAdjustDetailEntity::getWarehouseId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<String> vmIds = detailList.stream().map(VirtualAdjustDetailEntity::getVirtualWarehouseId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        //获取虚拟仓对应的实体仓库存
        VirtualInventoryDTO.ParamDTO params = new VirtualInventoryDTO.ParamDTO();
        params.setSkuIdList(skuIds);
        params.setWarehouseIdList(warehouseIds);
        List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryRealList = virtualInventoryService.getRealQty(params);
        //获取实体库存
        List<String> inventoryStatusList = Arrays.asList(InventoryStatusEnum.USABLE.getCode(), InventoryStatusEnum.FROZEN.getCode());
        List<InventoryDTO.RealQtyDTO> inventoryRealList = inventoryService.getRealQty(skuIds,warehouseIds,inventoryStatusList);
        //根据仓库+sku分组
        Map<String, List<VirtualInventoryDTO.ViewQtyDTO>> groupMap = virtualInventoryRealList.stream().collect(Collectors.groupingBy(item -> item.getWarehouseId() + item.getSkuId()));
        groupMap.forEach((key, value) -> {
            //获取调整单明细数据
            List<VirtualAdjustDetailEntity> detailEntities = detailList.stream().filter(detail -> detail.getWarehouseId().equals(value.get(0).getWarehouseId()) && detail.getSkuId().equals(value.get(0).getSkuId())).collect(Collectors.toList());
            //调整单明细数据求和
            Integer adjustQty = detailEntities.stream().mapToInt(VirtualAdjustDetailEntity::getQty).sum();
            //虚拟库存求和
            Integer virtualRealQty = virtualInventoryRealList.stream().mapToInt(VirtualInventoryDTO.ViewQtyDTO::getToVirtualWarehouseRealQty).sum();
            //实体库存求和
            Integer realQty = inventoryRealList.stream().mapToInt(InventoryDTO.RealQtyDTO::getRealQty).sum();
            //调整虚拟仓库存+虚拟仓实际库存 > 实体库存 报错
            if (adjustQty + virtualRealQty > realQty){
                throw new ServiceException("调整后的虚拟仓【{}】SKU【{}】虚拟库存大于实体【{}】库存【{}】",detailEntities.get(0).getVirtualWarehouseName(),detailEntities.get(0).getSkuNo(),detailEntities.get(0).getWarehouseName(),realQty);
            }
        });
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualAdjustDetailEntity virtualAdjustDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
