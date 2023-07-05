package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpFbaDeliveryDetailService;
import com.common.core.controller.vo.ApiResult;


/**
 * 
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@RestController
@RequestMapping("/dmpFbaDeliveryDetail")
public class DmpFbaDeliveryDetailController extends BaseController {

    @Autowired
    private DmpFbaDeliveryDetailService dmpFbaDeliveryDetailService;



}
