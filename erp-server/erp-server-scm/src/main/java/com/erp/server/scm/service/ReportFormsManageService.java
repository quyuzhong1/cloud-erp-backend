package com.erp.server.scm.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ReportFormsManageService {
    /**
     * 采购业务汇总表列表查询
     * @Author Luo_WG
     * @Date 2023/6/13 10:19
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO.PagingViewDTO>
     **/
    PagingVO<List<PurchaseBusinessGatherTableDTO.PagingViewDTO>> purchaseBusinessGatherTablPaginge(PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> dto);

    /**
     * 导出excel
     * @Author Luo_WG
     * @Date 2023/6/13 19:47
     * @param dto
     * @param response
     * @return java.lang.Boolean
     **/
    Boolean exportExcelPurchaseBusiness(PurchaseBusinessGatherTableDTO.PagingParamDTO dto, HttpServletResponse response);
}
