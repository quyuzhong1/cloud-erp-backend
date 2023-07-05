package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpShipmentDetailService;
import com.common.core.controller.vo.ApiResult;


/**
 * 调拨发货明细
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@RestController
@RequestMapping("/dmpShipmentDetail")
public class DmpShipmentDetailController extends BaseController {

    @Autowired
    private DmpShipmentDetailService dmpShipmentDetailService;



}
