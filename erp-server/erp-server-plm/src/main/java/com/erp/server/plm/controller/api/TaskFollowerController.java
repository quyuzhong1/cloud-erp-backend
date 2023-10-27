package com.erp.server.plm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.TaskFollowerDTO;
import com.erp.server.plm.service.TaskFollowerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 任务列表-任务评论
 *
 * @author Lambda
 * @since 2023-06-19
 */
@RestController
@LogSystemModule("任务列表")
@RequestMapping("/taskFollower")
public class TaskFollowerController extends BaseController {


    @Resource
    private TaskFollowerService taskFollowerService;


    /**
     * 获取当前用户关注改任务信息【PLM1.3】
     *
     * @param taskId
     * @return
     */
    @GetMapping("getTaskFollower")
    public ApiResult<TaskFollowerDTO.InfoDTO> getFollowerByTaskId(@RequestParam("taskId") String taskId) {
        TaskFollowerDTO.InfoDTO result = taskFollowerService.getFollowerByTaskId(taskId);
        return success(result);
    }

    /**
     * 关注任务【PLM1.3】
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "关注任务:任务id={taskId}")
    @PostMapping("follower")
    public ApiResult followerTask(@RequestBody @Valid TaskFollowerDTO.FollowerDTO dto) {
        Boolean follower = taskFollowerService.followerTask(dto);
        return follower?success():failure();
    }

    /**
     * 取消关注【PLM1.3】
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "取消关注:任务id={taskId}")
    @PostMapping("cancelFollower")
    public ApiResult cancelFollower(@RequestBody @Valid TaskFollowerDTO.FollowerDTO dto) {
        Boolean result = taskFollowerService.cancelFollower(dto);
        return result?success():failure();
    }


}
