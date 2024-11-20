package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.mrp.dto.LocalHistoryInventoryDTO;
import com.erp.model.mrp.dto.LocalHistoryInventoryGroupDTO;
import com.erp.model.mrp.entity.LocalHistoryInventoryEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.mapper.LocalHistoryInventoryMapper;
import com.erp.server.mrp.service.LocalHistoryInventoryService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 本地仓库存 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-11-08
 */
@Service
public class LocalHistoryInventoryServiceImpl extends SuperServiceImpl<LocalHistoryInventoryMapper, LocalHistoryInventoryEntity> implements LocalHistoryInventoryService {
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private InventoryService inventoryService;
    @Override
    public void saveTodayInventory(List<InventoryEntity> localHistoryInventory, LocalDate calculationDate) {
        List<LocalHistoryInventoryEntity> entityList = list(Wrappers.<LocalHistoryInventoryEntity>lambdaQuery()
                .eq(LocalHistoryInventoryEntity::getBillDate, calculationDate));
        //获取所有的在途库存
        List<InventoryReportDTO.TransportPagingDTO> dtos = inventoryService.listLocalInTransit(calculationDate);
        Map<LocalHistoryInventoryGroupDTO, List<InventoryEntity>> groupDTOListMap = localHistoryInventory.stream()
                .collect(Collectors.groupingBy(v -> new LocalHistoryInventoryGroupDTO(v.getOrgId(), v.getWarehouseId(), v.getSkuId(), v.getSkuNo())));
        List<LocalHistoryInventoryEntity> entities = groupDTOListMap.entrySet().parallelStream()
                .map(v -> {
                    LocalHistoryInventoryEntity inventory = entityList.stream()
                            .filter(e -> v.getKey().getSkuId().equals(e.getSkuId()))
                            .filter(e -> v.getKey().getWarehouseId().equals(e.getWarehouseId()))
                            .filter(e -> v.getKey().getOrgId().equals(e.getOrgId()))
                            .findFirst()
                            .orElse(new LocalHistoryInventoryEntity());
                    buildBasicAttribute(calculationDate, v, inventory);
                    // 合并待质检，可用，冻结库存
                    mergeInventory(v, inventory);
                    //获取在途库存
                    InventoryReportDTO.TransportPagingDTO dto = dtos.stream()
                            .filter(e -> v.getKey().getSkuId().equals(e.getSkuId()))
                            .filter(e -> v.getKey().getWarehouseId().equals(e.getWarehouseId()))
                            .filter(e -> v.getKey().getOrgId().equals(e.getOrgId()))
                            .findFirst()
                            .orElse(new InventoryReportDTO.TransportPagingDTO());
                    inventory.setPurchaseTransitQty(Integer.parseInt(Optional.ofNullable(dto.getPurchaseQty()).orElse("0")));
                    inventory.setTransferTransitQty(Optional.ofNullable(dto.getTransferQty()).orElse(0));
                    return inventory;
                }).collect(Collectors.toList());
        ApplicationContextUtils.getBean(LocalHistoryInventoryServiceImpl.class).saveOrUpdateBatch(entities);
    }

    /**
     * 合并库存数据
     * @param v 库存
     * @param inventory 库存
     */
    private void mergeInventory(Map.Entry<LocalHistoryInventoryGroupDTO, List<InventoryEntity>> v, LocalHistoryInventoryEntity inventory) {
        for (InventoryEntity entity : v.getValue()) {
            if (InventoryStatusEnum.USABLE.getCode().equals(entity.getDictInventoryStatus())) {
                inventory.setUsableQty(Optional.ofNullable(inventory.getUsableQty()).orElse(0) + entity.getQty());
            } else if (InventoryStatusEnum.WAIT_QC.getCode().equals(entity.getDictInventoryStatus())) {
                inventory.setUsableQty(Optional.ofNullable(inventory.getWaitqcQty()).orElse(0) + entity.getQty());
            }else if (InventoryStatusEnum.FROZEN.getCode().equals(entity.getDictInventoryStatus())) {
                inventory.setUsableQty(Optional.ofNullable(inventory.getFrozenQty()).orElse(0) + entity.getQty());
            }
        }
    }

    /**
     * 构建基础属性
     * @param calculationDate 计算日
     * @param v 库存
     * @param inventory 历史库存
     */
    private void buildBasicAttribute(LocalDate calculationDate, Map.Entry<LocalHistoryInventoryGroupDTO, List<InventoryEntity>> v, LocalHistoryInventoryEntity inventory) {
        inventory.setOrgId(v.getKey().getOrgId());
        inventory.setWarehouseId(v.getKey().getWarehouseId());
        inventory.setSkuNo(v.getKey().getSkuNo());
        inventory.setSkuId(v.getKey().getSkuId());
        inventory.setBillDate(calculationDate);
    }

    @Override
    public PagingVO<LocalHistoryInventoryDTO.PagingViewDTO> paging(PagingDTO<LocalHistoryInventoryDTO.SearchParamDTO> dto) {
        Page<LocalHistoryInventoryDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<LocalHistoryInventoryDTO.PagingViewDTO> pageData = baseMapper.paging(query, dto.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())){
            return new PagingVO<>();
        }
        //填充分页数据
        filList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void filList(List<LocalHistoryInventoryDTO.PagingViewDTO> records) {
        List<String> warehouseId = records.stream()
                .map(LocalHistoryInventoryDTO.PagingViewDTO::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntityList = FeignQuery.getByIds(WarehouseEntity.class, warehouseId);
        Map<String, String> warehouseMap = warehouseEntityList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName, (o1,o2) -> o1));

        //查询产品信息
        List<String> skuIdList = records.stream()
                .map(LocalHistoryInventoryDTO.PagingViewDTO::getSkuId)
                .distinct()
                .collect(Collectors.toList());

        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, String> skuVOMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName, (o1,o2) -> o1));
        for (LocalHistoryInventoryDTO.PagingViewDTO data : records) {
            data.setProductName(skuVOMap.get(data.getSkuId()));
            data.setWarehouseName(warehouseMap.get(data.getWarehouseId()));
        }
    }

    @Override
    public void exportExcel(LocalHistoryInventoryDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("本地仓每日库存", FileTaskEventEnum.EXPORT_MRP_LOCAL_INVENTORY.getCode(), dto);
    }

    @Override
    public PagingVO<LocalHistoryInventoryDTO.PagingViewDTO> exportLocalInventory(PagingDTO<LocalHistoryInventoryDTO.ExportDTO> dto) {
        IPage<LocalHistoryInventoryDTO.PagingViewDTO> pageData = baseMapper.exportLocalInventory(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())){
            return new PagingVO<>();
        }
        //填充分页数据
        filList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }
}
