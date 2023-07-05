package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpSkuCostService;
import com.common.core.controller.vo.ApiResult;


/**
 * <p>
 * sku bom关系表
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
@RestController
@RequestMapping("/dmpSkuCost")
public class DmpSkuCostController extends BaseController {

    @Autowired
    private DmpSkuCostService dmpSkuCostService;



}
