package com.erp.server.oms.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.OrderCategoryEntity;
import com.erp.server.oms.service.OrderCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 订单分类表
 *
 * @author Lambda
 * @since 2023-08-24
 */
@RestController
@RequestMapping("/order-category-entity")
public class OrderCategoryController extends BaseController {

    @Resource
    private OrderCategoryService orderCategoryService;


    /**
     * 获取订单分类列表
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<OrderCategoryEntity>> list() {
        List<OrderCategoryEntity> list = orderCategoryService.list();
        return success(list);
    }

}
