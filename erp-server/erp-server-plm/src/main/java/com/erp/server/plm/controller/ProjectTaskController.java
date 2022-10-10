package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.server.plm.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;
import java.util.Map;

/**
  产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/task")
public class ProjectTaskController extends BaseController {

    @Autowired
    private ProjectTaskService taskService;

    /**
     * 项目任务-分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> paging(@RequestBody @Validated PagingDTO<TaskPagingDTO> dto) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = taskService.paging(dto);
        return success(pagingVO);
    }

    @PostMapping("/save")
    public ApiResult save(@RequestBody @Validated ProjectTaskDTO dto) {
        Boolean flag = taskService.save(dto);
        return flag == true ? success() : failure();
    }

    @PostMapping("/saveSonTask")
    public ApiResult saveSonTask(@RequestBody @Validated ProjectTaskDTO dto) {
        Boolean flag = taskService.save(dto);
        return flag == true ? success() : failure();
    }

    @GetMapping("/list")
    public ApiResult list(@RequestBody @Validated BasicProductIdDTO dto) {
        List<Map<String, Object>> list = taskService.getTaskListByProductId(dto);
        return success(list);
    }

    @PostMapping("/remove")
    public ApiResult remove(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = taskService.removeTask(dto.getId());
        return flag == true ? success() : failure();
    }

    @PostMapping("/setPreTask")
    public ApiResult setPreTask(@RequestBody @Validated SetPreTaskDTO dto) {
        Boolean flag = taskService.setPreTask(dto);
        return flag == true ? success() : failure();
    }

    @PostMapping("/removePreTask")
    public ApiResult removePreTask(@RequestBody @Validated BaseIdDTO dto) {
        SetPreTaskDTO taskDTO = new SetPreTaskDTO();
        taskDTO.setTaskId(dto.getId());
        taskDTO.setPreTaskId("");
        Boolean flag = taskService.setPreTask(taskDTO);
        return flag == true ? success() : failure();
    }


}

