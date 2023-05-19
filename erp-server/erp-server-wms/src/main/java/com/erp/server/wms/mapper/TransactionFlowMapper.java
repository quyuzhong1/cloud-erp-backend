package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.entity.TransactionFlowEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname: TransactionFlowMapper
 * @Description: TODO
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

}
