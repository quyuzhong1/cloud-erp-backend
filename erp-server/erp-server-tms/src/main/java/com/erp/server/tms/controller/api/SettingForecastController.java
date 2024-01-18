package com.erp.server.tms.controller.api;


import com.common.core.controller.vo.ApiResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

/**
 * 预报设置
 * @author Lambda
 * @since 2024-01-18
 */
@RestController
@RequestMapping("/forecastSetting")
public class SettingForecastController extends BaseController {


    @PostMapping("list")
    public ApiResult  list(){

    }

}
