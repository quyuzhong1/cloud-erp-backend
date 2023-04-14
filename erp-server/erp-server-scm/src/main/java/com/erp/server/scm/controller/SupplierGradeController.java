package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.validator.ValidList;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierGradeEntity;
import com.erp.server.scm.service.SupplierGradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 供应商管理
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier/grade")
public class SupplierGradeController extends BaseController {


    @Resource
    private SupplierGradeService supplierGradeService;

    /**
     * 批量保存供应商等级
     *
     * @param gradeList
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Valid ValidList<SupplierDTO.SupplierGradeDTO> gradeList) {
        Boolean result = supplierGradeService.saveOrUpdateBatchGrade(gradeList);
        return result == true ? success() : failure();
    }


    /**
     * 供应商等级列表
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<SupplierDTO.SupplierGradeDTO>> saveOrUpdate() {
        List<SupplierGradeEntity> list = supplierGradeService.list();
        List<SupplierDTO.SupplierGradeDTO> result = BeanMapper.copyList(list, SupplierDTO.SupplierGradeDTO.class);
        return success(result);
    }

    /**
     * 检查供应商等级能否删除
     *
     * @return
     */
    @PostMapping("/checkDelete")
    public ApiResult checkDelete(@RequestBody @Valid BaseIdDTO dto) {
        Boolean result = supplierGradeService.checkDelete(dto.getId());
        return result?success():failure();
    }


}
