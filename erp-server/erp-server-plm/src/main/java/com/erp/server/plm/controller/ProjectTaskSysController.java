package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysTaskDTO;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.ProjectTaskSysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.erp.common.controller.BaseController;

/**
 * <p>
 * 系统任务 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/sys/task")
public class ProjectTaskSysController extends BaseController {

    @Autowired
    private ProjectTaskSysService projectTaskSysService;


    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SysTaskDTO dto) {
        Boolean result = projectTaskSysService.saveOrUpdateSysTask(dto);
        return result == true ? success() : failure();
    }

    @PostMapping("/paging")
    public ApiResult paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO pagingVO = projectTaskSysService.paging(dto);
        return success(pagingVO);
    }

    @PostMapping("/remove")
    public ApiResult paging(String taskId) {
        Boolean flag=projectTaskSysService.removeTask(taskId);
        return flag==true?success():failure();
    }

}

