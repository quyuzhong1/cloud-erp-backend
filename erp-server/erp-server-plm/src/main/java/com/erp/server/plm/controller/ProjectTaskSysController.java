package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysTaskDTO;
import com.erp.model.plm.dto.SysTaskPagingDTO;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.ProjectTaskSysService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;
import java.util.Map;

/**
 * 产品系统通用设置
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/sys/task")
public class ProjectTaskSysController extends BaseController {

    @Autowired
    private ProjectTaskSysService projectTaskSysService;

    /**
     * 新建或者修改任务
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SysTaskDTO dto) {
        Boolean result = projectTaskSysService.saveOrUpdateSysTask(dto);
        return result == true ? success() : failure();
    }


    /**
     * 分页获取系统任务
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SysTaskPagingDTO>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<SysTaskPagingDTO> pagingVO = projectTaskSysService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 删除任务
     */
    @PostMapping("/remove")
    public ApiResult paging(String taskId) {
        Boolean flag=projectTaskSysService.removeTask(taskId);
        return flag==true?success():failure();
    }

    /**
     * 新建任务 获取前置任务列表
     */
    @GetMapping("/list")
    public ApiResult list() {
        List<Map<String,Object>> list= projectTaskSysService.taskList();
        return success(list);
    }

}

