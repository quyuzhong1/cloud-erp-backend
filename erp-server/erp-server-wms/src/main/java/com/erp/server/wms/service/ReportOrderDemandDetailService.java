package com.erp.server.wms.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReportOrderDemandDetailDTO;
import com.erp.model.wms.entity.ReportOrderDemandDetailEntity;

/**
 * <p>
 * 订单需求明细报表 服务类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
public interface ReportOrderDemandDetailService extends SuperService<ReportOrderDemandDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReportOrderDemandDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    Boolean update(ReportOrderDemandDetailDTO.UpdateDTO dto);

    /**
     * 列表查询
     * @author will
     * @date 2024/9/23 17:27
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ReportOrderDemandDetailDTO.ListDTO> paging(PagingDTO<ReportOrderDemandDetailDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/9/23 17:30
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(ReportOrderDemandDetailDTO.PagingParamDTO dto);
    /**
     * 导出查询数据
     * @author will
     * @date 2024/9/24 12:19
     * @param dto
     * @return List<ListDTO>
     */
    PagingVO<ReportOrderDemandDetailDTO.ListDTO> listReportOrderDemandDetail(PagingDTO<ReportOrderDemandDetailDTO.PagingParamDTO> dto);
}
