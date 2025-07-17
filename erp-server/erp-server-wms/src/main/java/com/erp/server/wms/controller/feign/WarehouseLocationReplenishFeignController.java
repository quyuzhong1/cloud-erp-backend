package com.erp.server.wms.controller.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.server.wms.service.WarehouseLocationReplenishService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 仓位补货 Feign 接口
 * @date 2024-06-25
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/warehouseLocationReplenish")
public class WarehouseLocationReplenishFeignController {

    @Resource
    private WarehouseLocationReplenishService warehouseLocationReplenishService;

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

    /**
     * 查询仓位补货的tab
     * @param
     * @return
     * @date: 2025-06-09
     * @author: jack
     */
    @GetMapping("/listTabInfo")
    List<WarehouseLocationReplenishDTO.TabDTO> listTabInfo(){
        return warehouseLocationReplenishService.listTabInfo();
    }
}
