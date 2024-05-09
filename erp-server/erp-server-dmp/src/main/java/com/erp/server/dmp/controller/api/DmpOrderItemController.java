package com.erp.server.dmp.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.dmp.service.DmpOrderItemService;
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
public class DmpOrderItemController extends BaseController {
    @Resource
    private DmpOrderItemService dmpOrderItemService;

    @GetMapping("/init")
    public ApiResult<String> initItem(){
        dmpOrderItemService.initItem();
        return ApiResult.success("");
    }

}
