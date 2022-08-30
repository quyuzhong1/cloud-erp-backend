package com.cloud.erp.chrome.controller;


import com.cloud.erp.chrome.dto.YxkOrderDTO;
import com.cloud.erp.chrome.service.YxkOrderService;
import com.erp.common.dto.base.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.erp.common.controller.BaseController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-08-29
 */
@RestController
@RequestMapping("yxk/api")
public class YxkOrderController extends BaseController {

    @Autowired
    private YxkOrderService  yxkOrderService;

    @PostMapping("/importFile")
    public ApiResult importFile(@RequestBody @Validated YxkOrderDTO dto){
        yxkOrderService.saveYxkOrder(dto);
        return success();
    }

}

