package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.entity.TransactionFlowEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname: TransactionFlowMapper

 * @CreateTime: 2023-04-25  19:44
 * @Author: zhangchunlin
 */
@Repository
@Mapper
public interface TransactionFlowMapper extends BaseMapper<TransactionFlowEntity> {

    /**
     * 修改交易流水为已反审核
     * @param id
     * @param version
     * @return
     */
    int updateUnapprovedById(@Param(value = "id") String id, @Param(value = "version") Integer version,
                      @Param(value = "updateTime") LocalDateTime updateTime, @Param(value = "updateUserId") String updateUserId, @Param(value = "updateUserName") String updateUserName);


    /**
     * 分页查询即时库存对应的流水
     * @param query
     * @param params
     * @return
     */
    IPage<InventoryDTO.TransFlowPagingViewDTO> pagingForInv(Page query, @Param("params") InventoryDTO.TransFlowSearchParamDTO params);

    /**
     * 出入库列表导出
     * @param params
     * @return
     */
    List<InventoryDTO.TransFlowPagingViewDTO> exportTransFlow(@Param("params") InventoryDTO.ExportInvFlowSearchParamDTO params);


    /**
     * 出入库流水分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<InventoryDTO.InOutStockTransFlowPagingViewDTO> paging(Page query, @Param("params") InventoryDTO.InOutStockTransFlowSearchParamDTO params);


    /**
     * 出入库流水导出查询
     * @param params
     * @return
     */
    List<InventoryDTO.InOutStockTransFlowPagingViewDTO> exportList(@Param("params") InventoryDTO.ExportInOutStockTransFlowSearchParamDTO params);


    /**
     * 出入库列表分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<InventoryDTO.InOutStockSummaryPagingViewDTO> pagingList(Page query, @Param("params") InventoryDTO.InOutStockSummarySearchParamDTO params);

    /**
     * 出入库列表导出
     * @param params
     * @return
     */
    List<InventoryDTO.InOutStockSummaryPagingViewDTO> exportSummaryList(@Param("params") InventoryDTO.ExcelInOutStockSummarySearchParamDTO params);

    /**
     * 在途查询分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<InventoryReportDTO.TransportPagingDTO> transportPagingList(Page query, @Param("params") InventoryReportDTO.TransportSearchParamDTO params);


    /**
     * 在途查询导出查询
     * @param params
     * @return
     */
    List<InventoryReportDTO.TransportPagingDTO> exportTransport(@Param("params") InventoryReportDTO.ExportTransportSearchParamDTO params);

    /**
     * 在途查询单据明细分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<InventoryReportDTO.ListTransportPagingDTO> transportList(Page query, @Param("params") InventoryReportDTO.ListTransportSearchParam params);

    /**
     * @description: 每日库存分页查询
     * @author Will
     * @date: 2023/12/6 17:18
     * @param query
     * @param params
     * @return IPage<ListDailyInventoryDTO>
     */
    IPage<InventoryReportDTO.ListDailyInventoryDTO> dailyInventoryPaging(Page query, @Param("params") InventoryReportDTO.DailyInventoryParamDTO params);

    /**
     * 只查询库存数量
     * @param params
     * @return
     */
    List<InventoryReportDTO.ListDailyInventoryDTO> listDailyInventoryQty(@Param("params") InventoryReportDTO.DailyInventoryParamDTO params);

    /**
     * 查询存在流水的库存id
     * @param startDate 执行开始时间
     * @param orgId   组织id
     * @param inventoryId 库存id
     * @return List<String>
     */
    List<String> listByOrgId(@Param("startDate") LocalDate startDate, @Param("orgId") String orgId, @Param("inventoryId") String inventoryId);
}
