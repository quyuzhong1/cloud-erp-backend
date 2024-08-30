package com.erp.server.sys.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.DictThirdCityService;

/**
 * 第三方城市字典表
 *
 * @author lrp
 * @since 2023-11-23
 */
@Slf4j
@RestController
@LogSystemModule("第三方城市字典表")
@RequestMapping("/dictThirdCity")
public class DictThirdCityController extends BaseController {

    @Resource
    private DictThirdCityService dictThirdCityService;

}
