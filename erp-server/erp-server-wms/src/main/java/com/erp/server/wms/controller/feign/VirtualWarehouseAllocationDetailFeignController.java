package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.server.wms.service.VirtualWarehouseAllocationDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author hyj
 */
@RestController
@RequestMapping("feign/virtualWarehouseAllocationDetail")
public class VirtualWarehouseAllocationDetailFeignController {

    @Resource
    VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;


    @PostMapping("/updateSyncStatus")
    public void updateSyncStatus(@RequestBody VirtualWarehouseAllocationDTO.SyncUpdateDto dto) {
        virtualWarehouseAllocationDetailService.updateSyncStatus(dto);
    }

}

