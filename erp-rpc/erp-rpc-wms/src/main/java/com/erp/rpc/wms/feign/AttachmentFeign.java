package com.erp.rpc.wms.feign;


import com.erp.model.wms.dto.WmsAttachmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-wms", contextId = "attachment")
public interface AttachmentFeign {

    /**
     * 仓库设备新增附件
     * @param dto
     */
    @PostMapping(value = "/feign/attachment/saveAttachment")
    void addByWarehouseEquipment(@RequestBody WmsAttachmentDTO.AddDTO dto);
}
