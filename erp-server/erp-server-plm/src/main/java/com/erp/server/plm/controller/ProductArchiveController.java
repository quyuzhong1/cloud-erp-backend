package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.server.plm.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname 产品归档
 * @Description TODO
 * @Date 2022-10-08 14:59
 * @Created by yl
 */
@RestController
@RequestMapping("plm/product/archive")
public class ProductArchiveController extends BaseController {
    @Autowired
    private ProductInfoService productInfoService;

    @PostMapping("/paging")
    public ApiResult paging(@RequestBody @Validated PagingDTO<ProductSearchDTO> dto) {
        PagingVO pagingVO = productInfoService.archivePaging(dto);
        return success(pagingVO);
    }
}
