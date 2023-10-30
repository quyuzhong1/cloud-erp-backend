package com.erp.server.plm.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.TaskCommentDTO;
import com.erp.server.plm.service.TaskCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务列表-任务评论
 *
 * @Classname TaskCommentController

 * @Date 2022-10-13 17:51
 * @Created by yl
 */

@RestController
@LogSystemModule("任务列表")
@RequestMapping("taskComment")
public class TaskCommentController extends BaseController {

    @Autowired
    private TaskCommentService taskCommentService;


    /**
     * 任务详情-添加评论【PLM1.3】
     *
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加评论")
    @PostMapping("/save")
    public ApiResult saveTaskComment(@RequestBody @Validated TaskCommentDTO.AddDTO dto) {
        Boolean result = taskCommentService.saveTaskComment(dto);
        return result ? success() : failure();
    }

    /**
     * 任务详情-任务评论列表【PLM1.3】
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<TaskCommentDTO.ListDTO>> listByTaskId(@RequestParam("taskId") String taskId) {
        List<TaskCommentDTO.ListDTO> resultList = taskCommentService.listByTaskId(taskId);
        return success(resultList);
    }
}
