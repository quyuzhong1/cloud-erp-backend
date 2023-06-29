package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpShipmentService;
import com.common.core.controller.vo.ApiResult;


/**
 * FBA调拨发货
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@RestController
@RequestMapping("/dmpShipment")
public class DmpShipmentController extends BaseController {

    @Autowired
    private DmpShipmentService dmpShipmentService;



}
