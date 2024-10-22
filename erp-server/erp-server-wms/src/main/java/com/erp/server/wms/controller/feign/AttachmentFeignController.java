package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.server.wms.service.WmsAttachmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("/feign/attachment")
public class AttachmentFeignController extends BaseController {

    @Resource
    private WmsAttachmentService wmsAttachmentService;


    @PostMapping("/saveAttachment")
    void addByWarehouseEquipment(@RequestBody WmsAttachmentDTO.AddDTO dto) {
        wmsAttachmentService.addByWarehouseEquipment(dto);
    }
}
