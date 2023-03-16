package com.erp.server.scm.controller;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierGradeDTO;
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
     * 保存或者修改供应商等级
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SupplierGradeDTO dto) {
        return success();
    }


    /**
     * 供应商等级列表
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<SupplierGradeDTO>> saveOrUpdate() {
        return success();
    }

}
