package com.erp.server.oms.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.OmsAttachmentDTO;
import com.erp.server.oms.service.OmsAttachmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 附件管理
 * @Classname OmsAttachmentController

 * @Date 2023-05-17 17:44
 * @Created by yl
 */
@RestController
@LogSystemModule("OMS系统")
@RequestMapping("/attachment")
public class OmsAttachmentController  extends BaseController {
    @Resource
    private OmsAttachmentService omsAttachmentService;

    /**
     * 删除附件信息
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除附件信息")
    @PostMapping("/delete")
    public ApiResult<Object> removeAttachment(@RequestBody OmsAttachmentDTO.DeleteDTO dto) {
        omsAttachmentService.removeAttachment(dto);
        return success();
    }


}
