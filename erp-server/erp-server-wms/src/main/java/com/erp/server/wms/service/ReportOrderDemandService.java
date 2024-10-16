package com.erp.server.wms.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReportOrderDemandDTO;
import com.erp.model.wms.entity.ReportOrderDemandEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
public interface ReportOrderDemandService extends SuperService<ReportOrderDemandEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-09-23
    * @param addOrUpdateList
    * @return
    */
    Boolean batchAddOrUpdate(List<ReportOrderDemandDTO.AddDTO> addOrUpdateList);

    /**
     * 分页查询
     * @author will
     * @date 2024/9/23 17:43
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ReportOrderDemandDTO.ListDTO> paging(PagingDTO<ReportOrderDemandDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/9/23 17:43
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(ReportOrderDemandDTO.PagingParamDTO dto);
    /**
     * 导出数据查询
     * @author will
     * @date 2024/9/24 12:20
     * @param dto
     * @return List<ListDTO>
     */
    PagingVO<ReportOrderDemandDTO.ListDTO> listReportOrderDemand(PagingDTO<ReportOrderDemandDTO.PagingParamDTO> dto);
    /**
     * 单个查看分货
     * @author will
     * @date 2024/9/25 10:48
     * @param dto
     * @return ApiResult<ViewAllocationDTO>
     */
    ReportOrderDemandDTO.ViewVirtualAllocationDTO viewAllocation(ReportOrderDemandDTO.ViewVirtualAllocationParamDTO dto);
    /**
     * 单个分货新增保存
     * @author will
     * @date 2024/9/25 11:03
     * @param dto
     * @return Boolean
     */
    Boolean addAllocation(ReportOrderDemandDTO.AddVirtualAllocationDTO dto);
    /**
     * 批量分货查看
     * @author will
     * @date 2024/9/25 11:26
     * @param list
     * @return List<BatchViewVirtualAllocationDTO>
     */
    List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO> batchViewAllocation(ValidList<ReportOrderDemandDTO.ViewVirtualAllocationParamDTO> list);
    /**
     * 批量分货新增保存
     * @author will
     * @date 2024/9/25 11:28
     * @param list
     * @return Boolean
     */
    Boolean batchAddAllocation(ValidList<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> list);
}
