package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.entity.SupplierPhaseEntity;

import java.util.List;

/**
 * <p>
 * 供应商升降级 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierPhaseService extends SuperService<SupplierPhaseEntity> {

    
    
    /**
     * 添加供应商阶段
     * @author yl
     * @date 2023-03-23 12:20
     * @param dto
     * @return com.erp.model.scm.entity.SupplierPhaseEntity
     */
    SupplierPhaseEntity add(SupplierPhaseDTO.AddDTO dto);

    /**
     * 提交并审核
     * @author yl
     * @date 2023-03-23 16:34
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SupplierPhaseDTO.AddDTO dto);

    /**
     * 供应商提交审核
     * @author yl
     * @date 2023-03-23 16:50
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    
    /**
     * 供应商详情阶段
     * @author yl
     * @date 2023-03-23 17:12
     * @param id
     * @return com.erp.model.scm.dto.SupplierPhaseDTO.UpdateDTO
     */
    SupplierPhaseDTO.UpdateDTO view(String id);

    /**
     * 更改供应商阶段
     * @author yl
     * @date 2023-03-23 17:23
     * @param dto
     * @return com.erp.model.scm.entity.SupplierPhaseEntity
     */
    Boolean updateSupplierPhase(SupplierPhaseDTO.UpdateDTO dto);

    
    /**
     * 审核供应商阶段
     * @author yl
     * @date 2023-03-23 17:43
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    
    /**
     * 删除供应商阶段
     * @author yl
     * @date 2023-03-23 17:54
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    /**
     * 取消流程
     * @author yl
     * @date 2023-03-23 17:58
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 分页查询
     * @author yl
     * @date 2023-03-23 18:15
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierPhaseDTO.PagingViewDTO>
     */
    PagingVO<SupplierPhaseDTO.PagingViewDTO> paging(PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto);
}
