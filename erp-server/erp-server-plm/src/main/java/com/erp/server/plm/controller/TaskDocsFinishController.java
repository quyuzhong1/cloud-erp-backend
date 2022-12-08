package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.model.plm.dto.TaskChangeFileDTO;
import com.erp.model.plm.dto.TaskUploadFileDTO;
import com.erp.server.plm.service.TaskDocsFinishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

/**
 *产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/finish/docs")
public class TaskDocsFinishController extends BaseController {

    @Autowired
    private TaskDocsFinishService taskDocsFinishService;

    /**
     * 项目任务-任务详情-上传文件
     * @author yl
     * @date 2022-10-14 11:05
     * @param dto
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/importFile")
    //  @RequestPermissions("plm:finish:docs:importFile")
    public ApiResult uploadFile(@ModelAttribute @Validated TaskUploadFileDTO dto) {
        Boolean flag = taskDocsFinishService.uploadFile(dto);
        return flag == true ? success() : failure();
    }




    /**
     * 项目任务-任务详情-删除文件
     * @author yl
     * @date 2022-10-14 11:05
     * @param
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/removeFile")
    // @RequestPermissions("plm:finish:docs:removeFile")
    public ApiResult removeFile(@RequestParam(value = "finishDocsId") String finishDocsId) {
        Boolean flag = taskDocsFinishService.removeDocs(finishDocsId);
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情-变更文档
     * @author yl
     * @date 2022-10-14 11:05
     * @param
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/changeFile")
    //   @RequestPermissions("plm:finish:docs:changeFile")
    public ApiResult changeFile(@ModelAttribute @Validated TaskChangeFileDTO dto) {
        Boolean flag = taskDocsFinishService.changeFile(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 任务列表-变更文档
     * @author yl
     * @date 2022-10-14 11:05
     * @param
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/updateFile")
    //   @RequestPermissions("plm:finish:docs:changeFile")
    public ApiResult updateFile(@ModelAttribute @Validated TaskChangeFileDTO dto) {
        Boolean flag = taskDocsFinishService.updateFile(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 任务列表-变更文档-发起变更文档流程
     * @author yl
     * @date 2022-11-14 18:40
     * @param dto
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/startChangeDocsProcess")
    //   @RequestPermissions("plm:finish:docs:changeFile")
    public ApiResult startChangeDocsProcess(@Validated BaseIdDTO dto) {
        Boolean flag = taskDocsFinishService.startChangeDocsProcess(dto);
        return flag == true ? success() : failure();
    }



}

