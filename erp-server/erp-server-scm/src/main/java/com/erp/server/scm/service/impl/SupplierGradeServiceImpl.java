package com.erp.server.scm.service.impl;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierGradeEntity;
import com.erp.server.scm.mapper.SupplierGradeMapper;
import com.erp.server.scm.service.SupplierGradeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        List<SupplierGradeEntity> dbList = this.list();
        //检查名称
        checkName(gradeList, dbList);
        List<SupplierGradeEntity> batchGradeList = new ArrayList<>(gradeList.size());
        batchGradeList = BeanMapper.copyList(gradeList, SupplierGradeEntity.class);
        //获取到删除的 等级id
        List<String> deleteIdList = getDeleteIds(gradeList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        return this.saveOrUpdateBatch(batchGradeList);
    }


    /**
     * 获取到删除的数据
     *
     * @param gradeList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-17 11:45
     */
    private List<String> getDeleteIds(List<SupplierDTO.SupplierGradeDTO> gradeList, List<SupplierGradeEntity> dbList) {
        List<String> ids = gradeList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SupplierDTO.SupplierGradeDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SupplierGradeEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    /**
     * 检查等级名是否存在
     *
     * @param gradeList
     * @return void
     * @author yl
     * @date 2023-03-17 11:25
     */
    private void checkName(List<SupplierDTO.SupplierGradeDTO> gradeList, List<SupplierGradeEntity> dbList) {


        //这个是参数传来的名称
        List<String> nameList = gradeList.stream().map(SupplierDTO.SupplierGradeDTO::getName).
                collect(Collectors.toList());

        //这个是数据库包含的
        List<SupplierGradeEntity> containsNameList = dbList.stream().filter(d -> nameList.contains(d.getName())).collect(Collectors.toList());
        int count = 0;
        for (SupplierGradeEntity item : containsNameList) {
            String name = item.getName();
            String id = gradeList.stream().filter(g -> g.getName().equals(name)).findFirst().flatMap(obj ->
                    Optional.ofNullable(obj.getId())).orElse("");
            if (!item.getId().equals(id)) {
                count++;
            }
        }

        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98000);
        }

    }
}
