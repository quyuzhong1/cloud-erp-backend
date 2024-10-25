package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.FbaInventoryEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * fba历史库存 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
public interface FbaHistoryInventoryService extends SuperService<FbaHistoryInventoryEntity> {

    /**
     * 保存每日库存
     * @param inventoryEntities 库存
     * @param calculationDate 日期
     */
    void saveTodayInventory(List<FbaInventoryEntity> inventoryEntities, LocalDate calculationDate);

    List<FbaHistoryInventoryEntity> listBySkuNo(String skuNo, String fbaWarehouseId);

    /**
     * 分页查询
     * @param dto 分页参数
     */
    PagingVO<FbaHistoryInventoryDTO.ListDTO> paging(PagingDTO<FbaHistoryInventoryDTO.PagingParamDTO> dto);

    /**
     * 导出
     * @param dto 入参
     */
    void exportList(FbaHistoryInventoryDTO.ExportDTO dto);
}
