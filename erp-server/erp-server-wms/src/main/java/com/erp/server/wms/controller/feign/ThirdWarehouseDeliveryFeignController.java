package com.erp.server.wms.controller.feign;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.GenerateDeliveryAndOutStockDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 三方仓发货
 *
 */
@Slf4j
@RestController
@LogSystemModule("三方仓发货")
@RequestMapping("/feign/thirdWarehouseDelivery")
public class ThirdWarehouseDeliveryFeignController extends BaseController {

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    @PostMapping("/add")
    public ThirdWarehouseDeliveryEntity add(@RequestBody ThirdWarehouseDeliveryEntity entity) {
        return thirdWarehouseDeliveryService.add(entity);
    }

    @PostMapping("/getLatestBySoId")
    public ThirdWarehouseDeliveryEntity getLatestBySoId(@RequestBody String soId) {
        return thirdWarehouseDeliveryService.getLatestBySoId(soId);
    }

    @PostMapping("/update")
    public boolean update(@RequestBody ThirdWarehouseDeliveryEntity entity) {
       return thirdWarehouseDeliveryService.updateById(entity);
    }
    @PostMapping("/generateDeliveryAndOutStock")
    public  void generateDeliveryAndOutStock(@RequestBody GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO) {
        thirdWarehouseDeliveryService.generateDeliveryAndOutStock(generateDeliveryAndOutStockDTO);
    }
}
