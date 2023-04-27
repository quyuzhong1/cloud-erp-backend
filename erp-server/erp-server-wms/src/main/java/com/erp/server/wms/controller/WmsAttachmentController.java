package com.erp.server.wms.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.server.wms.service.WmsAttachmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 附件管理
 * @author Lambda
 * @Classname AttachmentController
 * @Description TODO
 * @Date 2023-04-03 14:05
 * @Created by yl
 */
@RestController
@RequestMapping("/attachment")
public class WmsAttachmentController extends BaseController {

    @Resource
    private WmsAttachmentService wmsAttachmentService;


    /**
     * 删除附件信息
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult removeAttachment(@RequestBody AttachmentDTO.DeleteDTO dto) {
        wmsAttachmentService.removeAttachment(dto);
        return success();
    }
}
