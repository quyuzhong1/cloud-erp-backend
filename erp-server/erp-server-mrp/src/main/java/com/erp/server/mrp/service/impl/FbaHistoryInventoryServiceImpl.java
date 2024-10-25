package com.erp.server.mrp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.enums.DeliveryChannelsEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.mapper.FbaHistoryInventoryMapper;
import com.erp.server.mrp.service.FbaHistoryInventoryService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
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
        List<FbaHistoryInventoryEntity> entityList = list(Wrappers.<FbaHistoryInventoryEntity>lambdaQuery().eq(FbaHistoryInventoryEntity::getBillDate, calculationDate));
        List<FbaHistoryInventoryEntity> entities = inventoryEntities.parallelStream()
                .map(v -> {
                    FbaHistoryInventoryEntity inventory = entityList.stream()
                            .filter(e -> v.getWarehouseId().equals(e.getWarehouseId()))
                            .filter(e -> v.getAsin().equals(e.getAsin()))
                            .filter(e -> v.getMsku().equals(e.getMsku()))
                            .filter(e -> v.getFnSku().equals(e.getFnSku()))
                            .findFirst()
                            .orElse(new FbaHistoryInventoryEntity());
                    v.setId(null);
                    BeanUtils.copyProperties(v, inventory);
                    inventory.setBillDate(calculationDate);
                    inventory.setId(inventory.getId());
                    return inventory;
                }).collect(Collectors.toList());
        saveOrUpdateBatch(entities);
    }

    @Override
    public List<FbaHistoryInventoryEntity> listBySkuNo(String skuNo, String fbaWarehouseId) {
        return list(Wrappers.<FbaHistoryInventoryEntity>lambdaQuery()
                .eq(FbaHistoryInventoryEntity::getSkuNo, skuNo)
                .eq(FbaHistoryInventoryEntity::getWarehouseId, fbaWarehouseId)
        );
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
        downloadTaskFeign.saveDownloadTask("FBA每日库存", FileTaskEventEnum.EXPORT_MRP_FBA_INVENTORY.getCode(),dto);
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
