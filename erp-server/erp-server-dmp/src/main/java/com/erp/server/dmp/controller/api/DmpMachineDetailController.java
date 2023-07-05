package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpMachineDetailService;
import com.common.core.controller.vo.ApiResult;


/**
 * 加工单明细
 *
 * @author Cloud
 * @since 2023-06-25
 */
@RestController
@RequestMapping("/dmpMachineDetail")
public class DmpMachineDetailController extends BaseController {

    @Autowired
    private DmpMachineDetailService dmpMachineDetailService;



}
