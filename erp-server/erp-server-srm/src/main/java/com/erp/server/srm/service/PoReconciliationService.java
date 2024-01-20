package com.erp.server.srm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.PoReconciliationDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 采购对账单 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
public interface PoReconciliationService extends SuperService<PoReconciliationEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PoReconciliationDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    Boolean update(PoReconciliationDTO.UpdateDTO dto);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/1/20 12:12
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<PoReconciliationDTO.ListDTO> paging(PagingDTO<PoReconciliationDTO.PagingParamDTO> dto);
    /**
     * @description: 导出
     * @author Will
     * @date: 2024/1/20 12:21
     * @param dto
     * @param response
     */
    void exportList(PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response);
}
