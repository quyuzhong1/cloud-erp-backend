package com.erp.server.wms.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.inventory.InOutStockCustomDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockRuleDTO;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.server.wms.service.InventoryTransCoreService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * @CreateTime: 2023-05-25  12:20
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

    @Autowired
    private InventoryTransCoreService inventoryTransCoreService;

    @GetMapping(value = "/test2")
    public ApiResult test() {
        InventoryInOutStockRuleDTO inventoryInOutStockRuleDTO = new InventoryInOutStockRuleDTO();
        inventoryInOutStockRuleDTO.setBusinessType(InventoryBusinessTypeEnum.INSTOCK_FORCAST.getCode());

        List<InOutStockCustomDTO> members = Lists.newArrayList();

        InOutStockCustomDTO inOutStockCustomDTO = new InOutStockCustomDTO();
        inOutStockCustomDTO.setSkuId("1654670183970766849");
        inOutStockCustomDTO.setSkuNo("A011CNA1");
        inOutStockCustomDTO.setBillDate(LocalDate.now());
        inOutStockCustomDTO.setSourceCode("TEST001");
        inOutStockCustomDTO.setSourceId("789");
        inOutStockCustomDTO.setSourceType(InventorySourceTypeEnum.INSTOCK_FORCAST);

        inventoryTransCoreService.approveByRule(inventoryInOutStockRuleDTO);
        return success();
    }


}