package com.erp.server.wms.service;


import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.entity.InitStockEntity;

/**
 * <p>
 * 期初库存表 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-10
 */
public interface InitStockService extends SuperService<InitStockEntity> {

    /**
     * 分页列表
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InitStockDTO.ListDTO> paging(PagingDTO<InitStockDTO.SearchParamDTO> pagingParamDTO);


    /**
     * 查看详情
     * @param id
     * @return
     */
    InitStockDTO.ViewDTO view(String id);

    /**
     * 导出Excel
     * @param param
     */
    void exportExcel(InitStockDTO.SearchParamDTO param);

}
