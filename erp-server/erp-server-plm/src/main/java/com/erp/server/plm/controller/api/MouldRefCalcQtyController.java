package com.erp.server.plm.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.plm.service.MouldRefCalcQtyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 模具返还数量计算 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-12-10
 */
@RestController
@RequestMapping("/mould-ref-calc-qty")
public class MouldRefCalcQtyController extends BaseController {

    @Resource
    private MouldRefCalcQtyService mouldRefCalcQtyService;

    @PostMapping("/calcRefundQty")
    public ApiResult<String> calcRefundQty() {
        mouldRefCalcQtyService.calcRefundQty();
        return success();
    }

}
