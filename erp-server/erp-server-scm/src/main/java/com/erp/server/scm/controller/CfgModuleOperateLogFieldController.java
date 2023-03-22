package com.erp.server.scm.controller;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.ModuleOperateLogDTO;
import com.erp.server.scm.service.CfgModuleOperateLogFieldService;
import com.erp.server.scm.service.ModuleOperateLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 操作日志
 *
 * @author will
 * @since 2023-03-17
 */
@RestController
@RequestMapping("/moduleOperateLogField")
public class CfgModuleOperateLogFieldController extends BaseController {

    @Resource
    private CfgModuleOperateLogFieldService cfgModuleOperateLogFieldService;

    /**
     * 操作日志-操作日志字段新增
     * @author Will
     * @date: 2023/3/22 13:26
     * @return ApiResult
     */
    @PostMapping("/saveBatchSysLogField")
    public ApiResult saveBatchSysLogField() {
        Boolean flag = cfgModuleOperateLogFieldService.saveBatchSysLogField();
        return flag == true ? success() : failure();
    }
}
