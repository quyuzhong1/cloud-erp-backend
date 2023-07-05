package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpFbaDeliveryService;
import com.common.core.controller.vo.ApiResult;


/**
 * FBA发货单
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@RestController
@RequestMapping("/dmpFbaDelivery")
public class DmpFbaDeliveryController extends BaseController {

    @Autowired
    private DmpFbaDeliveryService dmpFbaDeliveryService;



}
