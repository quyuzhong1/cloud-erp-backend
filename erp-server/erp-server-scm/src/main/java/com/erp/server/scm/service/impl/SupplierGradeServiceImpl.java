package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierGradeEntity;
import com.erp.server.scm.mapper.SupplierGradeMapper;
import com.erp.server.scm.service.SupplierGradeService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 供应商分类表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-15
 */
@Service
public class SupplierGradeServiceImpl extends SuperServiceImpl<SupplierGradeMapper, SupplierGradeEntity> implements SupplierGradeService {

    @Override
    public Boolean saveOrUpdateBatchGrade(List<SupplierDTO.SupplierGradeDTO> gradeList) {
        if (CollectionUtils.isEmpty(gradeList)) {
            return true;
        }
        List<SupplierGradeEntity> batchGradeList = new ArrayList<>(gradeList.size());
        batchGradeList = BeanMapper.copyList(gradeList, SupplierGradeEntity.class);
        return this.saveOrUpdateBatch(batchGradeList);
    }
}
