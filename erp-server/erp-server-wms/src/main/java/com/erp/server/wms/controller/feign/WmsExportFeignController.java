package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.server.wms.service.InventoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("feign/export")
public class WmsExportFeignController {

    @Resource
    private InventoryService inventoryService;

    @PostMapping("/getInventoryPageData")
    PagingVO<InventoryDTO.PagingViewDTO> getInventoryPageData(@RequestBody PagingDTO<InventoryDTO.ExportSearchParamDTO> dto) {
        return inventoryService.getInventoryPageData(dto);
    }
}
