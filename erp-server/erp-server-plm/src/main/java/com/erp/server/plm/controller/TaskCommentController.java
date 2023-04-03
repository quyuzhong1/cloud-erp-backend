package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.TaskCommentDTO;
import com.erp.model.plm.entity.TaskCommentEntity;
import com.erp.server.plm.service.TaskCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品开发管理
 *
 * @Classname TaskCommentController
 * @Description TODO
 * @Date 2022-10-13 17:51
 * @Created by yl
 */

@RestController
@RequestMapping("plm/taskComment")
public class TaskCommentController extends BaseController {

    @Autowired
    private TaskCommentService taskCommentService;


    /**
     * 项目任务-任务详情-添加评论
     *
     * @return
     */
    @PostMapping("/save")
    //   @RequestPermissions("plm:taskComment:save")
    public ApiResult saveTaskComment(@RequestBody @Validated TaskCommentDTO dto) {
        Boolean result = taskCommentService.saveTaskComment(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情-任务评论列表
     *
     * @return
     */
    @GetMapping("/list")
    // @RequestPermissions("plm:taskComment:list")
    public ApiResult saveTaskComment(String taskId) {
        List<TaskCommentEntity> result = taskCommentService.getListByTaskId(taskId);
        return success(result);
    }
}
