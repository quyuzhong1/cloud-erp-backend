package com.erp.server.scm.controller;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *供应商管理
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier/grade")
public class SupplierGradeController extends BaseController {

    /**
     * 批量保存供应商等级
     * @param gradeList
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated List<SupplierDTO.SupplierGradeDTO> gradeList) {
        return success();
    }


    /**
     * 供应商等级列表
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<SupplierDTO.SupplierGradeDTO>> saveOrUpdate() {
        return success();
    }

}
