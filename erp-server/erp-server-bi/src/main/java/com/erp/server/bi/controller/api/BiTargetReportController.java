package com.erp.server.bi.controller.api;

import com.common.core.controller.BaseController;
import com.erp.server.bi.service.BiTargetReportService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 目标相关报表控制层
 * @author Will
 * @version 1.0
 * @date 2023/9/14 12:16
 */
@RestController
@RequestMapping("/report")
public class BiTargetReportController extends BaseController {

    @Resource
    private BiTargetReportService biTargetReportService;

}
