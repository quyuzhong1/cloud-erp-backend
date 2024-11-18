package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.OverseasHistoryInventoryDTO;
import com.erp.model.mrp.entity.OverseasHistoryInventoryEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.OverseasInventoryEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 海外仓库存 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
public interface OverseasHistoryInventoryService extends SuperService<OverseasHistoryInventoryEntity> {

    /**
     * 保存每日库存
     * @param overseasHistoryInventory 海外库存
     * @param calculationDate 计算日期
     */
    void saveTodayInventory(List<OverseasInventoryEntity> overseasHistoryInventory, LocalDate calculationDate);

    /**
     * 分页查询
     * @param dto 分页参数
     */
    PagingVO<OverseasHistoryInventoryDTO.ListDTO> paging(PagingDTO<OverseasHistoryInventoryDTO.PagingParamDTO> dto);

    /**
     * 导出
     * @param dto 导出参数
     */
    void exportList(OverseasHistoryInventoryDTO.ExportDTO dto);

    /**
     * 根据开始结束时间查询
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    List<OverseasHistoryInventoryEntity> listByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);
}
