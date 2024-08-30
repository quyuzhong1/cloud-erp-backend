package com.erp.server.scm.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierReportDTO;

/**
 * @Classname: SupplierReportService
 * @Description: 供应商相关报表服务接口类
 * @CreateTime: 2023-06-16  16:45
 * @Author: zhangchunlin
 */
public interface SupplierReportService {

    /**
     * 供应商报表分页查询
     * @param paramDTO
     * @return
     */
    PagingVO<SupplierReportDTO.PagingViewDTO> supplierPaging(PagingDTO<SupplierReportDTO.PagingSearchParamDTO> paramDTO);

    /**
     * 导出
     *
     * @param paramDTO
     * @return
     */
    void exportList(SupplierReportDTO.ExportSearchParamDTO paramDTO);

    PagingVO<SupplierReportDTO.PagingViewDTO> exportSupplierReport(PagingDTO<SupplierReportDTO.ExportSearchParamDTO> dto);
}
