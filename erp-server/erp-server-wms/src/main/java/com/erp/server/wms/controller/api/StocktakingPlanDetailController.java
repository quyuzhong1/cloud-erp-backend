package com.erp.server.wms.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.StocktakingPlanDetailService;
import com.common.core.controller.vo.ApiResult;


/**
 * 盘点计划明细表
 *
 * @author Cloud
 * @since 2023-08-08
 */
@RestController
@RequestMapping("/stocktakingPlanDetail")
public class StocktakingPlanDetailController extends BaseController {

    @Autowired
    private StocktakingPlanDetailService stocktakingPlanDetailService;



}
