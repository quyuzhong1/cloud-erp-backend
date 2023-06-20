package com.erp.server.plm.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.TaskConcernDTO;
import com.erp.server.plm.service.TaskConcernService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 任务-任务关注人
 *
 * @author Lambda
 * @since 2023-06-19
 */
@RestController
@RequestMapping("/taskConcern")
public class TaskConcernController extends BaseController {


    @Resource
    private TaskConcernService taskConcernService;


    /**
     * 获取当前用户是否有关注改任务
     *
     * @param taskId
     * @return
     */
    @GetMapping("getTaskConcern")
    public ApiResult<TaskConcernDTO.InfoDTO> getConcernByTaskId(@RequestParam("taskId") String taskId) {
        TaskConcernDTO.InfoDTO result = taskConcernService.getConcernByTaskId(taskId);
        return success(result);
    }

    /**
     * 关注任务
     *
     * @param dto
     * @return
     */
    @PostMapping("concern")
    public ApiResult concernTask(@RequestBody @Valid TaskConcernDTO.ConcernDTO dto) {
        Boolean concern = taskConcernService.concernTask(dto);
        return concern?success():failure();
    }

    /**
     * 取消关注
     *
     * @param dto
     * @return
     */
    @PostMapping("cancelConcern")
    public ApiResult cancelConcern(@RequestBody @Valid TaskConcernDTO.ConcernDTO dto) {
        Boolean result = taskConcernService.cancelConcern(dto);
        return result?success():failure();
    }


}
