package com.erp.server.scm.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.server.scm.service.AttachmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 供应商管理
 * @author Lambda
 * @Classname AttachmentController

 * @Date 2023-04-03 14:05
 * @Created by yl
 */
@RestController
@LogSystemModule("供应商管理通用")
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
