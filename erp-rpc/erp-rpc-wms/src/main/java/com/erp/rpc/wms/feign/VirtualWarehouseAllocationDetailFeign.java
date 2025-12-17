package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * 虚拟仓分货单明细Feign
 *
 * @author hyj
 */
@FeignClient(name = "erp-wms", contextId = "virtualWarehouseAllocationDetail", path = "/feign/virtualWarehouseAllocationDetail",configuration = {FeignErrorDecoder.class})
public interface VirtualWarehouseAllocationDetailFeign {

    /**
     * 修改同步状态
     */
    @PostMapping("/updateSyncStatus")
    void updateSyncStatus(@RequestBody VirtualWarehouseAllocationDTO.SyncUpdateDto dto);

    /**
     * 获取明细记录
     * @param handelDetailId
     * @return
     */
    @GetMapping("/getByHandleDetailId")
    List<VirtualWarehouseAllocationDetailEntity> getByHandleDetailId(@RequestParam(value = "handelDetailId") String handelDetailId);
}


