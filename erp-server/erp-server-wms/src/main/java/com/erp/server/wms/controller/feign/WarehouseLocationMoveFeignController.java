package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.openapi.AiyaChangeAttributeDTO;
import com.erp.server.wms.service.WarehouseLocationMoveService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 仓位移动 Feign 控制器
 */
@RestController
@RequestMapping("/feign/warehouseLocationMove")
public class WarehouseLocationMoveFeignController extends BaseController {

    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;

    /**
     * 接收爱亚库存状态转移反馈，幂等生成已审核《仓位移动》。
     */
    @PostMapping("/receiveAiyaChangeAttribute")
    public String receiveAiyaChangeAttribute(@RequestBody @Validated AiyaChangeAttributeDTO dto) {
        return warehouseLocationMoveService.receiveAiyaChangeAttribute(dto);
    }
}
