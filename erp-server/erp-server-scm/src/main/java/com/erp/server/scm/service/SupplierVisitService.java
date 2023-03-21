package com.erp.server.scm.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.model.scm.entity.SupplierVisitEntity;

/**
 * <p>
 * 供应商拜访表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierVisitService extends SuperService<SupplierVisitEntity> {

    
    
    /**
     * 添加供应商拜访记录
     * @author yl
     * @date 2023-03-21 10:26
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean add(SupplierVisitDTO.AddDTO dto);

    
    /**
     * 获取到供应商拜访信息
     * @author yl
     * @date 2023-03-21 11:32
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierVisitDTO.PagingViewDTO>
     */
    PagingVO<SupplierVisitDTO.PagingViewDTO> paging(PagingDTO<BaseIdDTO> dto);
}
