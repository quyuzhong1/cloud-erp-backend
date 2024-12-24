package com.erp.server.mrp.controller.api;


import com.common.core.controller.vo.ApiResult;
import com.erp.server.mrp.service.SalesInfoService;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;

/**
 * <p>
 * 历史销量信息 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-09-25
 */
@RestController
@RequestMapping("/sales-info-history")
public class SalesInfoHistoryController extends BaseController {

    @Resource
    private SalesInfoService salesInfoService;

    @GetMapping("/dealHistorySaleQty")
    public ApiResult<String> dealHistorySaleQty() {

        salesInfoService.dealHistorySaleQty();
        return success();
    }

}
