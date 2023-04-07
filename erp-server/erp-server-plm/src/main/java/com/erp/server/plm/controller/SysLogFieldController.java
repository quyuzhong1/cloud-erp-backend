package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.plm.service.SysLogFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志
 * @author Will
 * @version 1.0
 * @date 2022/12/5 20:36
 */
@RestController
@RequestMapping("sys/logField")
public class SysLogFieldController extends BaseController {

    @Autowired
    private SysLogFieldService sysLogFieldService;

    /**
     * 操作日志-操作日志字段新增
     * @author Will
     * @date: 2022/12/7 13:26
     * @return ApiResult
     */
    @PostMapping("/saveBatchSysLogField")
    public ApiResult saveBatchSysLogField() {
       Boolean flag = sysLogFieldService.saveBatchSysLogField();
        return flag == true ? success() : failure();
    }

}
