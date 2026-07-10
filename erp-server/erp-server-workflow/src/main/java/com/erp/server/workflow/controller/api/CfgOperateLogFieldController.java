package com.erp.server.workflow.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.workflow.service.CfgOperateLogFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 日志字段配置表
 *
 * @author will
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("日志字段配置表")
@RequestMapping("/cfgOperateLogField")
public class CfgOperateLogFieldController extends BaseController {

    @Resource
    private CfgOperateLogFieldService cfgOperateLogFieldService;

    /**
     * 操作日志-操作日志字段新增
     * @author Will
     * @date: 2025/5/12 13:26
     * @return ApiResult
     */
    @PostMapping("/saveBatchSysLogField")
    public ApiResult<?> saveBatchSysLogField() {
        Boolean flag = cfgOperateLogFieldService.saveBatchSysLogField();
        return flag == true ? success() : failure();
    }

}
