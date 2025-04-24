package com.erp.server.oms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cRefCategoryService;
import com.common.core.controller.vo.ApiResult;


/**
 * B2C销售订单分类表
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@RestController
@RequestMapping("/soB2cRefCategory")
public class SoB2cRefCategoryController extends BaseController {

    @Resource
    private SoB2cRefCategoryService soB2cRefCategoryService;



}
