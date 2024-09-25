package com.erp.server.wms.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
import com.erp.model.wms.entity.ReportOrderSalesEntity;

/**
 * <p>
 * 订单销量表 服务类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
public interface ReportOrderSalesService extends SuperService<ReportOrderSalesEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReportOrderSalesDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    Boolean update(ReportOrderSalesDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2024/9/23 18:15
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ReportOrderSalesDTO.ListDTO> paging(PagingDTO<ReportOrderSalesDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/9/23 18:15
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(ReportOrderSalesDTO.PagingParamDTO dto);
    /**
     * 导出数据查询
     * @author will
     * @date 2024/9/24 12:21
     * @param dto
     * @return List<ListDTO>
     */
    PagingVO<ReportOrderSalesDTO.ListDTO> listReportOrderSales(PagingDTO<ReportOrderSalesDTO.PagingParamDTO> dto);
}
