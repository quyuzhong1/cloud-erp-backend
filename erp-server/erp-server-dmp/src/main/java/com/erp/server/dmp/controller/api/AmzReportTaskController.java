package com.erp.server.dmp.controller.api;


import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.AmzReportTaskService;

import lombok.extern.slf4j.Slf4j;

/**
 * 亚马逊报告请求记录
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@RestController
@LogSystemModule("亚马逊报告请求记录")
@RequestMapping("/amzReportTask")
public class AmzReportTaskController extends BaseController {

    @Resource
    private AmzReportTaskService amzReportTaskService;


}
