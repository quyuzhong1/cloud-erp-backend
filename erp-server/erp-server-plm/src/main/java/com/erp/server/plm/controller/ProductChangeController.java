package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.AddChangeDTO;
import com.erp.server.plm.service.ProductChangeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 变更信息表(ProductChange)表控制层
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@RestController
@RequestMapping("plm/change")
public class ProductChangeController extends BaseController {


    @Resource
    private ProductChangeService productChangeService;


    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated AddChangeDTO dto) {
        Boolean result = productChangeService.add(dto);
        return result == true ? success() : failure();
    }


}

