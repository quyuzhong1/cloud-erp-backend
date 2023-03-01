package com.erp.server.plm.controller;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.SysTaskDTO;
import com.erp.model.plm.dto.SysTaskPagingDTO;
import com.erp.model.plm.dto.SysTaskPagingSearchDTO;
import com.erp.model.plm.vo.SysTaskVO;
import com.erp.server.plm.service.ProjectTaskSysService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    //   @RequestPermissions("plm:sys:task:saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SysTaskDTO dto) {
        Boolean result = projectTaskSysService.saveOrUpdateSysTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 系统任务-编辑任务-获取任务详情
     *
     * @param taskId
     * @return
     */
    @GetMapping("/taskDetails")
    //  @RequestPermissions("plm:sys:task:taskDetails")
    public ApiResult<SysTaskVO> taskDetails(String taskId) {
        SysTaskVO taskVO = projectTaskSysService.taskDetails(taskId);
        return success(taskVO);
    }


    /**
     * 分页获取系统任务
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    //  @RequestPermissions("plm:sys:task:paging")
    public ApiResult<PagingVO<SysTaskPagingDTO>> paging(@RequestBody @Validated PagingDTO<SysTaskPagingSearchDTO> dto) {
        PagingVO<SysTaskPagingDTO> pagingVO = projectTaskSysService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 删除任务
     */
    @PostMapping("/remove")
    //  @RequestPermissions("plm:sys:task:remove")
    public ApiResult paging(String taskId) {
        Boolean flag=projectTaskSysService.removeTask(taskId);
        return flag==true?success():failure();
    }

    /**
     * 新建任务 获取前置任务列表
     */
    @GetMapping("/list")
    //  @RequestPermissions("plm:sys:task:list")
    public ApiResult list(@Param("templateId") String templateId) {
        List<Map<String,Object>> list= projectTaskSysService.taskList(templateId);
        return success(list);
    }

}

