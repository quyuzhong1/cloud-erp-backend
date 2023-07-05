package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpOutInStockDetailService;
import com.common.core.controller.vo.ApiResult;


/**
 * 手工出入库详情表
 *
 * @author Cloud
 * @since 2023-06-25
 */
@RestController
@RequestMapping("/dmpOutInStockDetail")
public class DmpOutInStockDetailController extends BaseController {

    @Autowired
    private DmpOutInStockDetailService dmpOutInStockDetailService;



}
