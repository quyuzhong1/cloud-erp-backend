package com.sdk.oms.shopify.chrome.controller;


import com.sdk.oms.shopify.chrome.dto.MabangOrderDTO;
import com.sdk.oms.shopify.chrome.service.YxkOrderService;
import com.common.core.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.common.core.controller.vo.ApiResult;

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
@CrossOrigin(origins = "*")
public class YxkOrderController extends BaseController {

    @Autowired
    private YxkOrderService yxkOrderService;

//    @PostMapping("/importFile")
//    public ApiResult importFile(@RequestBody @Validated YxkOrderDTO dto){
//        yxkOrderService.saveYxkOrder(dto);
//        return success();
//    }

    @PostMapping("/importFile")
    public ApiResult importFile(@ModelAttribute MabangOrderDTO dto){
        yxkOrderService.saveOrder(dto);
        return success();
    }

}

