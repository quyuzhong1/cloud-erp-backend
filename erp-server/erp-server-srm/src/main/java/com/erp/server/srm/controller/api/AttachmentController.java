package com.erp.server.srm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.srm.dto.AttachmentDTO;
import com.erp.server.srm.service.AttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    public ApiResult<Object> removeAttachment(@RequestBody AttachmentDTO.DeleteDTO dto) {
        attachmentService.removeAttachment(dto);
        return success();
    }



}
