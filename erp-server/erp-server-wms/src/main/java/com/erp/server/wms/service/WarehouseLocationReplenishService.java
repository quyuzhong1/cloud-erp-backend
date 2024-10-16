package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.model.wms.entity.WarehouseLocationReplenishEntity;

import java.util.List;

/**
 * 仓位库存预警服务接口
 * @date 2024-06-24
 * @author tanmujin
 */
public interface WarehouseLocationReplenishService extends SuperService<WarehouseLocationReplenishEntity> {
    /**
     * 仓位库存预警分页查询
     * @date 2024-06-24
     * @author tanmujin
     */
    PagingVO<WarehouseLocationReplenishDTO.ViewDTO> paging(PagingDTO<WarehouseLocationReplenishDTO.SearchParamDTO> pagingDTO);

    /**
     * 标记仓位库存预警无需处理
     * @param id
     * @return
     * @date: 2024-06-24
     * @author: tanmujin
     */
    BatchResultDTO cancelHandle(String id);

    /**
     * 导出仓位库存预警Excel
     *
     * @param dto 导出参数
     * @return void
     * @date: 2024-06-24
     * @author: tanmujin
     */
    Boolean exportExcel(WarehouseLocationReplenishDTO.ExportParamDTO dto);

    /**
     * 新增补货单
     * @param addDTO
     * @return
     * @date: 2024-06-26
     * @author: tanmujin
     */
    BatchResultDTO add(WarehouseLocationReplenishDTO.AddDTO addDTO);

    BatchResultDTO finish(WarehouseLocationReplenishDTO.HandleDTO dto);

    List<WarehouseLocationReplenishDTO.TabDTO> listTabInfo();

    List<WarehouseLocationReplenishDTO.LocationQtyDTO> listLocationQty(List<WarehouseLocationReplenishDTO.LocationQtyDTO> paramlist);

    List<BatchResultDTO> verifyReplenishQty(List<WarehouseLocationReplenishDTO.HandleDTO> dtoList);

    PagingVO<WarehouseLocationReplenishDTO.ViewDTO> exportWarehouseLocationReplenish(PagingDTO<WarehouseLocationReplenishDTO.ExportParamDTO> dto);
    /**
     * 安全库存补货
     * @date: 2024-09-26
     * @author: jack
     */
    List<BatchResultDTO> generateReplenishBill();
}
