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


    /**
     * 方法说明
     * @author yl
     * @date 2023-04-13 19:12
     * @param id
     * @return java.lang.Boolean
     */
    Boolean checkDelete(String id);

    /**
     * @description: 根据供应商id查询
     * @author Will
     * @date: 2023/4/25 19:46
     * @param supplierId
     * @return SupplierGradeEntity
     */
    SupplierGradeEntity getBySupplierId(String supplierId);
}
