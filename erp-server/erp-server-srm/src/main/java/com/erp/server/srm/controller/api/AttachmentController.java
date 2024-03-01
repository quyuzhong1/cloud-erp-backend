package com.erp.server.srm.controller.api;


import com.erp.model.oms.dto.OmsAttachmentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.srm.service.AttachmentService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.srm.dto.AttachmentDTO;

/**
 * 公共附件表
 *
 * @author will
 * @since 2024-01-20
 */
@Slf4j
@RestController
@LogSystemModule("公共附件表")
@RequestMapping("/attachment")
public class AttachmentController extends BaseController {

    @Resource
    private AttachmentService attachmentService;

    /**
     * 删除附件信息
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除附件信息")
    @PostMapping("/delete")
    public ApiResult removeAttachment(@RequestBody AttachmentDTO.DeleteDTO dto) {
        attachmentService.removeAttachment(dto);
        return success();
    }



}
