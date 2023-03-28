package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierGradeEntity;

import java.util.List;

/**
 * <p>
 * 供应商分类表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierGradeService extends SuperService<SupplierGradeEntity> {

    
    /**
     * 保存或者修改 供应商等级信息
     * @author yl
     * @date 2023-03-17 10:20
     * @param gradeList
     * @return java.lang.Boolean
     */
    Boolean saveOrUpdateBatchGrade(List<SupplierDTO.SupplierGradeDTO> gradeList);


}
