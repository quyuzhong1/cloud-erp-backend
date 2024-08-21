package com.erp.server.srm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;

import java.util.List;

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
    * 修改
    * @author will
    * @date: 2024-01-19
    * @param detailList
    * @param mainId
    * @return
    */
    Boolean update(List<PoReconciliationDetailDTO.UpdateDTO> detailList,String mainId);

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
    void exportList(PoReconciliationDetailDTO.PagingParamDTO dto);
}
