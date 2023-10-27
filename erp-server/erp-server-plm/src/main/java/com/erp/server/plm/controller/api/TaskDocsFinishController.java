package com.erp.server.plm.controller.api;


import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.TaskChangeFileDTO;
import com.erp.model.plm.dto.TaskUploadFileDTO;
import com.erp.server.plm.service.TaskDocsFinishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@LogSystemModule("产品开发管理")
@RequestMapping("finish/docs")
public class TaskDocsFinishController extends BaseController {

    @Autowired
    private TaskDocsFinishService taskDocsFinishService;

    /**
     * 项目任务-任务详情-上传文件
     *
     * @param dto
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-14 11:05
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "项目任务-任务详情-上传文件")
    @PostMapping("/importFile")
    public ApiResult uploadFile(@ModelAttribute @Validated TaskUploadFileDTO dto) {
        try {
            Boolean flag = taskDocsFinishService.uploadFile(dto);
            return flag == true ? success() : failure();
        } catch (Exception e) {
            System.out.println(e);
        }
        return failure();

    }


    /**
     * 项目任务-任务详情-删除文件
     *
     * @param
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-14 11:05
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "项目任务-任务详情-删除文件")
    @PostMapping("/removeFile")
    public ApiResult removeFile(@RequestParam(value = "finishDocsId") String finishDocsId) {
        Boolean flag = taskDocsFinishService.removeDocs(finishDocsId);
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情-变更文档【PLM1.3】
     *
     * @param
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-14 11:05
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目任务-任务详情-变更文档:任务id={taskId},产品id={productId}")
    @PostMapping("/changeFile")
    public ApiResult changeFile(@ModelAttribute @Validated TaskChangeFileDTO dto) {
        Boolean flag = taskDocsFinishService.changeFile(dto);
        return flag ? success() : failure();
    }



    /**
     * 任务列表-变更文档-发起变更文档流程
     *
     * @param dto
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-11-14 18:40
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "任务列表-变更文档-发起变更文档流程:id={id}")
    @PostMapping("/startChangeDocsProcess")
    public ApiResult startChangeDocsProcess(@Validated BaseIdDTO dto) {
        Boolean flag = taskDocsFinishService.startChangeDocsProcess(dto);
        return flag == true ? success() : failure();
    }


}

