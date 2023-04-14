package com.erp.server.wms.controller.feign;

import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/17 16:01
 */
@RestController
@RequestMapping("feign/warehouseReceive")
public class WarehouseReceiveFeignController {

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @PostMapping("/listWarehouseReceiveByPodIds")
    public List<WarehouseReceiveDetailEntity> listWarehouseReceiveByPodIds(@RequestBody List<String> purchaseDetailIds) {
        return warehouseReceiveDetailService.listWarehouseReceiveByPodIds(purchaseDetailIds);
    }



}
