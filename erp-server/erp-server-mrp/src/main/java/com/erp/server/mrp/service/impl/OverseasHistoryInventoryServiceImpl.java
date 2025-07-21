package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.OverseasHistoryInventoryDTO;
import com.erp.model.mrp.dto.OverseasHistoryInventoryGroupDTO;
import com.erp.model.mrp.entity.OverseasHistoryInventoryEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.mrp.mapper.OverseasHistoryInventoryMapper;
import com.erp.server.mrp.service.OverseasHistoryInventoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外仓库存 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
@Service
public class OverseasHistoryInventoryServiceImpl extends SuperServiceImpl<OverseasHistoryInventoryMapper, OverseasHistoryInventoryEntity> implements OverseasHistoryInventoryService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;


    @Override
    public void saveTodayInventory(List<OverseasInventoryEntity> overseasHistoryInventory, LocalDate calculationDate) {
        Map<OverseasHistoryInventoryGroupDTO.InventoryDTO, List<OverseasInventoryEntity>> groupDTOListMap = overseasHistoryInventory.stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getSkuNo()))
                .collect(Collectors.groupingBy(v -> {
                    OverseasHistoryInventoryGroupDTO.InventoryDTO groupDTO = new OverseasHistoryInventoryGroupDTO.InventoryDTO();
                    groupDTO.setDictPlatform(v.getDictPlatform());
                    groupDTO.setWarehouseCode(v.getWarehouseCode());
                    groupDTO.setSkuId(v.getSkuId());
                    return groupDTO;
                }));
        List<OverseasHistoryInventoryEntity> entityList = list(Wrappers.<OverseasHistoryInventoryEntity>lambdaQuery().eq(OverseasHistoryInventoryEntity::getBillDate, calculationDate));
        List<OverseasHistoryInventoryEntity> entities = groupDTOListMap.entrySet().parallelStream()
                .map(v -> {
                    OverseasHistoryInventoryEntity inventory = entityList.stream()
                            .filter(e -> v.getKey().getDictPlatform().equals(e.getDictPlatform()))
                            .filter(e -> v.getKey().getWarehouseCode().equals(e.getWarehouseCode()))
                            .filter(e -> v.getKey().getSkuId().equals(e.getSkuId()))
                            .filter(e -> calculationDate.equals(e.getBillDate()))
                            .findFirst()
                            .orElse(new OverseasHistoryInventoryEntity());
                    inventory.setWarehouseCode(v.getKey().getWarehouseCode());
                    inventory.setDictPlatform(v.getKey().getDictPlatform());
                    inventory.setSkuId(v.getKey().getSkuId());
                    addQty(v, inventory);
                    inventory.setBillDate(calculationDate);
                    return inventory;
                }).collect(Collectors.toList());
        ApplicationContextUtils.getBean(OverseasHistoryInventoryServiceImpl.class).saveOrUpdateBatch(entities);
    }

    @Override
    public PagingVO<OverseasHistoryInventoryDTO.ListDTO> paging(PagingDTO<OverseasHistoryInventoryDTO.PagingParamDTO> dto) {
        OverseasHistoryInventoryDTO.PagingParamDTO params = dto.getParams();
        Page<OverseasHistoryInventoryDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<OverseasHistoryInventoryDTO.ListDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        //填充分页数据
        filList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void filList(List<OverseasHistoryInventoryDTO.ListDTO> list) {
        //  查询仓库ID
        List<OverseasProviderDTO.ListWithWarehouseDTO> listWithWarehouseDTOS = overseasProviderFeign.listAllMatch();
        //查询产品信息
        List<String> skuIdList = list.stream()
                .map(OverseasHistoryInventoryDTO.ListDTO::getSkuId)
                .distinct()
                .collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, SkuVO> skuVOMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        for (OverseasHistoryInventoryDTO.ListDTO data : list) {
            if (StringUtils.isNotBlank(data.getSkuId())) {
                SkuVO skuVO = skuVOMap.get(data.getSkuId());
                if (null != skuVO) {
                    data.setProductName(skuVO.getSkuName());
                }
            }
            OverseasProviderDTO.ListWithWarehouseDTO warehouseDTO = listWithWarehouseDTOS.stream()
                    .filter(v -> v.getPlatformWarehouseCode().equals(data.getWarehouseCode()))
                    .filter(v -> v.getCode().equals(data.getDictPlatform()))
                    .findFirst()
                    .orElse(new OverseasProviderDTO.ListWithWarehouseDTO());
            data.setName(warehouseDTO.getWarehouseName());
        }
    }

    @Override
    public void exportList(OverseasHistoryInventoryDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("海外仓每日库存", FileTaskEventEnum.EXPORT_MRP_OVERSEAS_INVENTORY.getCode(), dto);
    }

    @Override
    public List<OverseasHistoryInventoryEntity> listByStartDateAndEndDate(LocalDate startDate, LocalDate endDate) {
        List<OverseasProviderDTO.ListWithWarehouseDTO> listWithWarehouseDTOS = overseasProviderFeign.listAllMatch();
        Map<String, String> warehouseCodeMap = listWithWarehouseDTOS.stream()
                .collect(Collectors.toMap(v -> v.getCode() + "-" + v.getPlatformWarehouseCode(), OverseasProviderDTO.ListWithWarehouseDTO::getWarehouseId,(firstValue, secondValue) -> firstValue));
        Set<String> warehouseCodes = listWithWarehouseDTOS.stream().map(OverseasProviderDTO.ListWithWarehouseDTO::getPlatformWarehouseCode).collect(Collectors.toSet());
        List<OverseasHistoryInventoryEntity> list = baseMapper.listByStartDateAndEndDate(startDate, endDate, warehouseCodes);
        for (OverseasHistoryInventoryEntity entity : list) {
            entity.setWarehouseId(warehouseCodeMap.get(entity.getDictPlatform() + "-" + entity.getWarehouseCode()));
        }
        return list;
    }

    @Override
    public PagingVO<OverseasHistoryInventoryDTO.ListDTO> exportOverseasInventory(PagingDTO<OverseasHistoryInventoryDTO.ExportDTO> dto) {
        Page<OverseasHistoryInventoryDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<OverseasHistoryInventoryDTO.ListDTO> pageData = baseMapper.exportOverseasInventory(query, dto.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        //填充分页数据
        filList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void addQty(Map.Entry<OverseasHistoryInventoryGroupDTO.InventoryDTO, List<OverseasInventoryEntity>> v, OverseasHistoryInventoryEntity inventory) {
        int deliverOnwayQty = MathUtil.ZERO;
        int pendingQty = MathUtil.ZERO;
        int sellableQty = MathUtil.ZERO;
        int unsellableQty = MathUtil.ZERO;
        int reservedQty = MathUtil.ZERO;
        int onwayQty = MathUtil.ZERO;
        int lackQty = MathUtil.ZERO;
        int frozenQty = MathUtil.ZERO;
        int shippedQty = MathUtil.ZERO;
        for (OverseasInventoryEntity entity : v.getValue()) {
            inventory.setSkuNo(entity.getSkuNo());
            inventory.setDownloadTime(entity.getDownloadTime());
            inventory.setName(entity.getName());
            deliverOnwayQty = deliverOnwayQty + entity.getDeliverOnwayQty();
            pendingQty = pendingQty + entity.getPendingQty();
            sellableQty = sellableQty + entity.getSellableQty();
            unsellableQty = unsellableQty + entity.getUnsellableQty();
            reservedQty = reservedQty + entity.getReservedQty();
            onwayQty = onwayQty + entity.getOnwayQty();
            lackQty = lackQty + entity.getLackQty();
            frozenQty = frozenQty + entity.getFrozenQty();
            shippedQty = shippedQty + entity.getShippedQty();
        }
        inventory.setDeliverOnwayQty(deliverOnwayQty);
        inventory.setPendingQty(pendingQty);
        inventory.setSellableQty(sellableQty);
        inventory.setUnsellableQty(unsellableQty);
        inventory.setReservedQty(reservedQty);
        inventory.setOnwayQty(onwayQty);
        inventory.setLackQty(lackQty);
        inventory.setFrozenQty(frozenQty);
        inventory.setShippedQty(shippedQty);
    }
}


