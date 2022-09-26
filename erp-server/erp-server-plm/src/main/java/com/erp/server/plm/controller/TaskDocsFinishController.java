package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.TaskUploadFileDTO;
import com.erp.server.plm.service.TaskDocsFinishService;
import org.apache.tools.ant.taskdefs.Apt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.erp.common.controller.BaseController;

/**
 * <p>
 * 任务文档交付表 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/finish/docs")
public class TaskDocsFinishController extends BaseController {

    @Autowired
    private TaskDocsFinishService taskDocsFinishService;

    @PostMapping("/importFile")
    public ApiResult uploadFile(@ModelAttribute @Validated TaskUploadFileDTO dto) {
        Boolean flag = taskDocsFinishService.uploadFile(dto);
        return flag == true ? success() : failure();
    }

}

