package com.erp.server.tms.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.tms.service.CfgOperateLogFieldService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 日志字段配置表
 *
 * @author will
 * @since 2023-05-08
 */
@RestController
@RequestMapping("/cfgOperateLogField")
public class CfgOperateLogFieldController extends BaseController {

    @Resource
    private CfgOperateLogFieldService cfgOperateLogFieldService;

    /**
     * 操作日志-操作日志字段新增
     * @author Will
     * @date: 2023/3/22 13:26
     * @return ApiResult
     */
    @PostMapping("/saveBatchSysLogField")
    public ApiResult<String> saveBatchSysLogField() {
        Boolean flag = cfgOperateLogFieldService.saveBatchSysLogField();
        return flag == true ? success() : failure();
    }

}
