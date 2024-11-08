package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.LocalHistoryInventoryDTO;
import com.erp.model.mrp.entity.LocalHistoryInventoryEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.InventoryEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 本地仓库存 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-11-08
 */
public interface LocalHistoryInventoryService extends SuperService<LocalHistoryInventoryEntity> {

    /**
     * 保存每日本地库存
     * @param localHistoryInventory 本地历史库存
     * @param calculationDate 计算日期
     */
    void saveTodayInventory(List<InventoryEntity> localHistoryInventory, LocalDate calculationDate);

    /**
     * 分页
     * @param dto 参数
     */
    PagingVO<LocalHistoryInventoryDTO.PagingViewDTO> paging(PagingDTO<LocalHistoryInventoryDTO.SearchParamDTO> dto);

    /**
     * 导出
     * @param dto 参数
     */
    void exportExcel(LocalHistoryInventoryDTO.ExportDTO dto);
}
