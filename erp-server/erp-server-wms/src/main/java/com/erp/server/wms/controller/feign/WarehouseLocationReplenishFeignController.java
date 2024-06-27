package com.erp.server.wms.controller.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import org.springframework.web.bind.annotation.*;

/**
 * 仓位补货 Feign 接口
 * @date 2024-06-25
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/warehouseLocationReplenish")
public class WarehouseLocationReplenishFeignController {

    /**
     * 新增补货单
     * @param
     * @return
     * @date: 2024-06-25
     * @author: tanmujin
     */
    @PostMapping("/addReplenishBill")
    public ApiResult<?> addReplenishBill(@RequestBody WarehouseLocationReplenishDTO.AddDTO addDto){

        return null;
    }
}
