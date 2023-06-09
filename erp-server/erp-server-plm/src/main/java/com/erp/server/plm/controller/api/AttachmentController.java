package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 附件表
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@RestController
@RequestMapping("/attachment")
public class AttachmentController extends BaseController {

    @Autowired
    private AttachmentService attachmentService;



}
