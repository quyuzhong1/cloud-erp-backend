package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpOutInStockService;
import com.common.core.controller.vo.ApiResult;


/**
 * 手工出入库待同步数据表
 *
 * @author Cloud
 * @since 2023-06-25
 */
@RestController
@RequestMapping("/dmpOutInStock")
public class DmpOutInStockController extends BaseController {

    @Autowired
    private DmpOutInStockService dmpOutInStockService;



}
