package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysLogSelectDTO;
import com.erp.model.plm.dto.SysLogShowDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.service.SysLogFieldService;
import com.erp.server.plm.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志
 * @author Will
 * @version 1.0
 * @date 2022/12/5 20:36
 */
@RestController
@RequestMapping("/plm/sys/logField")
public class SysLogFieldController extends BaseController {

    @Autowired
    private SysLogFieldService sysLogFieldService;

    /**
     * 操作日志-操作日志字段新增
     * @author Will
     * @date: 2022/12/7 13:26
     * @return ApiResult
     */
    @GetMapping("/saveBatchSysLogField")
    public ApiResult saveBatchSysLogField() {
       Boolean flag = sysLogFieldService.saveBatchSysLogField();
        return flag == true ? success() : failure();
    }

}
