package com.erp.server.dmp.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpAmzReportInfoService;

/**
 * 亚马逊报告请求记录
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@RestController
@LogSystemModule("亚马逊报告请求记录")
@RequestMapping("/amzReportInfo")
public class AmzReportInfoController extends BaseController {

    @Resource
    private DmpAmzReportInfoService dmpAmzReportInfoService;


}
