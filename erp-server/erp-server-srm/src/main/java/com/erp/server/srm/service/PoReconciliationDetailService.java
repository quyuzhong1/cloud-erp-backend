package com.erp.server.srm.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 采购对账单明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
public interface PoReconciliationDetailService extends SuperService<PoReconciliationDetailEntity> {

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/1/20 11:31
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<PoReconciliationDetailDTO.ListDTO> paging(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto);
    /**
     * @param dto
     * @description: 导出
     * @author Will
     * @date: 2024/1/20 12:03
     */
    void exportList(PoReconciliationDetailDTO.PagingParamDTO dto, HttpServletResponse response);
}
