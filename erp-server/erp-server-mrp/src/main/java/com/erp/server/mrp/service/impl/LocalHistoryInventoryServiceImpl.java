package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.mrp.dto.LocalHistoryInventoryDTO;
import com.erp.model.mrp.dto.OverseasHistoryInventoryDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.mrp.entity.LocalHistoryInventoryEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.mapper.LocalHistoryInventoryMapper;
import com.erp.server.mrp.service.LocalHistoryInventoryService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    @Override
    public void saveTodayInventory(List<InventoryEntity> localHistoryInventory, LocalDate calculationDate) {
//        List<LocalHistoryInventoryEntity> entityList = list(Wrappers.<LocalHistoryInventoryEntity>lambdaQuery().eq(LocalHistoryInventoryEntity::getBillDate, calculationDate));
//        List<LocalHistoryInventoryEntity> entities = localHistoryInventory.parallelStream()
//                .map(v -> {
//                    LocalHistoryInventoryEntity inventory = entityList.stream()
//                            .filter(e -> v.getSkuId().equals(e.getSkuId()))
//                            .filter(e -> v.getWarehouseId().equals(e.getWarehouseId()))
//                            .filter(e -> v.getDictInventoryStatus().equals(e.getDictInventoryStatus()))
//                            .filter(e -> v.getWarehouseLocation().equals(e.getWarehouseLocation()))
//                            .filter(e -> v.getOrgId().equals(e.getOrgId()))
//                            .findFirst()
//                            .orElse(new LocalHistoryInventoryEntity());
//                    v.setId(null);
//                    BeanUtils.copyProperties(v, inventory);
//                    inventory.setBillDate(calculationDate);
//                    inventory.setId(inventory.getId());
//                    return inventory;
//                }).collect(Collectors.toList());
//        saveOrUpdateBatch(entities);
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
}
