package com.erp.server.plm.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.ProductCustomsService;
import com.common.core.controller.vo.ApiResult;


/**
 * <p>
 * 目的国海关编码
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
 */
@RestController
@RequestMapping("/productCustoms")
public class ProductCustomsController extends BaseController {

    @Autowired
    private ProductCustomsService productCustomsService;



}
