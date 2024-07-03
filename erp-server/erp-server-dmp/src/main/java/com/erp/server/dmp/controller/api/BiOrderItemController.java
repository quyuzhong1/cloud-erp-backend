package com.erp.server.dmp.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.dmp.service.BiOrderItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 订单商品信息拆分前表 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2024-05-06
 */
@RestController
@RequestMapping("/order-item")
public class BiOrderItemController extends BaseController {
    @Resource
    private BiOrderItemService biOrderItemService;

    @GetMapping("/init")
    public ApiResult<String> initItem(){
        biOrderItemService.initItem();
        return ApiResult.success("");
    }

}
