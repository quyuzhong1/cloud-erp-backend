package com.erp.server.mrp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.model.mrp.dto.FbaHistoryInventoryGroupDTO;
import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.mapper.FbaHistoryInventoryMapper;
import com.erp.server.mrp.service.FbaHistoryInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * fba历史库存 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@Service
public class FbaHistoryInventoryServiceImpl extends SuperServiceImpl<FbaHistoryInventoryMapper, FbaHistoryInventoryEntity> implements FbaHistoryInventoryService {

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public void saveTodayInventory(List<FbaInventoryEntity> inventoryEntities, LocalDate calculationDate) {
        Map<FbaHistoryInventoryGroupDTO, List<FbaInventoryEntity>> groupDTOListMap = inventoryEntities.stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getSkuNo()))
                .collect(Collectors.groupingBy(v -> {
                    FbaHistoryInventoryGroupDTO groupDTO = new FbaHistoryInventoryGroupDTO();
                    groupDTO.setSkuNo(v.getSkuNo());
                    groupDTO.setWarehouseId(v.getWarehouseId());
                    return groupDTO;
                }));
        List<FbaHistoryInventoryEntity> entityList = list(Wrappers.<FbaHistoryInventoryEntity>lambdaQuery().eq(FbaHistoryInventoryEntity::getBillDate, calculationDate));
        List<FbaHistoryInventoryEntity> entities = groupDTOListMap.entrySet().parallelStream()
                .map(v -> {
                    FbaHistoryInventoryEntity inventory = entityList.stream()
                            .filter(e -> v.getKey().getWarehouseId().equals(e.getWarehouseId()))
                            .filter(e -> v.getKey().getSkuNo().equals(e.getSkuNo()))
                            .filter(e -> calculationDate.equals(e.getBillDate()))
                            .findFirst()
                            .orElse(new FbaHistoryInventoryEntity());
                    inventory.setSkuNo(v.getKey().getSkuNo());
                    inventory.setWarehouseId(v.getKey().getWarehouseId());
                    addQty(v, inventory);
                    inventory.setBillDate(calculationDate);
                    return inventory;
                }).collect(Collectors.toList());
        ApplicationContextUtils.getBean(FbaHistoryInventoryServiceImpl.class).saveOrUpdateBatch(entities);
    }

    /**
     * 合并数量
     * @param v 参数
     * @param inventory 库存
     */
    private void addQty( Map.Entry<FbaHistoryInventoryGroupDTO, List<FbaInventoryEntity>> v,
                         FbaHistoryInventoryEntity inventory) {
        int fbmFulfillableQty = MathUtil.ZERO;
        int inboundWorkingQty = MathUtil.ZERO;
        int inboundShippedQty = MathUtil.ZERO;
        int inboundReceivingQty = MathUtil.ZERO;
        int fulfillableQty = MathUtil.ZERO;
        int reservedQty = MathUtil.ZERO;
        int researchingQty = MathUtil.ZERO;
        int unsellableQty = MathUtil.ZERO;
        for (FbaInventoryEntity entity : v.getValue()) {
            inventory.setWarehouseName(entity.getWarehouseName());
            fbmFulfillableQty = fbmFulfillableQty + entity.getFbmFulfillableQty();
            inboundWorkingQty = inboundWorkingQty + entity.getInboundWorkingQty();
            inboundShippedQty = inboundShippedQty + entity.getInboundShippedQty();
            inboundReceivingQty = inboundReceivingQty + entity.getInboundReceivingQty();
            fulfillableQty = fulfillableQty + entity.getFulfillableQty();
            reservedQty = reservedQty + entity.getReservedQty();
            researchingQty = researchingQty + entity.getResearchingQty();
            unsellableQty = unsellableQty + entity.getUnsellableQty();
        }
        inventory.setFbmFulfillableQty(fbmFulfillableQty);
        inventory.setInboundWorkingQty(inboundWorkingQty);
        inventory.setInboundShippedQty(inboundShippedQty);
        inventory.setInboundReceivingQty(inboundReceivingQty);
        inventory.setFulfillableQty(fulfillableQty);
        inventory.setReservedQty(reservedQty);
        inventory.setResearchingQty(researchingQty);
        inventory.setUnsellableQty(unsellableQty);
    }

    @Override
    public List<FbaHistoryInventoryEntity> listByStartDateAndEndDate(LocalDate startDate, LocalDate endDate) {
        return list(Wrappers.<FbaHistoryInventoryEntity>lambdaQuery()
                .ne(FbaHistoryInventoryEntity::getSkuNo, "")
                .between(FbaHistoryInventoryEntity::getBillDate, startDate, endDate));
    }

    @Override
    public PagingVO<FbaHistoryInventoryDTO.ListDTO> paging(PagingDTO<FbaHistoryInventoryDTO.PagingParamDTO> dto) {
        Page<FbaHistoryInventoryDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FbaHistoryInventoryDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>();
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(FbaHistoryInventoryDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("FBA每日库存", FileTaskEventEnum.EXPORT_MRP_FBA_INVENTORY.getCode(), dto);
    }

    @Override
    public PagingVO<FbaHistoryInventoryDTO.ListDTO> exportFbaInventory(PagingDTO<FbaHistoryInventoryDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<FbaHistoryInventoryDTO.ListDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }


    private void fillList(List<FbaHistoryInventoryDTO.ListDTO> records) {
        List<String> skuNoList = records.stream().map(FbaHistoryInventoryDTO.ListDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
        for (FbaHistoryInventoryDTO.ListDTO record : records) {
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(record.getSkuNo())).findFirst().orElse(new SkuVO());
            record.setProductName(skuVO.getSkuName());
        }
    }
}
