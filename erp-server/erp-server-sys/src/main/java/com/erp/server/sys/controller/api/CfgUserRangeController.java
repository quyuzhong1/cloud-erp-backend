package com.erp.server.sys.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.CfgUserRangeService;
import com.common.core.controller.vo.ApiResult;


/**
 * <p>
 * 用户区间配置表
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-12
 */
@RestController
@RequestMapping("/cfgUserRange")
public class CfgUserRangeController extends BaseController {

    @Autowired
    private CfgUserRangeService cfgUserRangeService;



}
