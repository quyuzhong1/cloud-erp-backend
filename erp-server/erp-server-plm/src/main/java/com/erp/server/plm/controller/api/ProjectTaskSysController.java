package com.erp.server.plm.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
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
@LogSystemModule("系统通用设置")
@RequestMapping("sys/task")
public class ProjectTaskSysController extends BaseController {

    @Autowired
    private ProjectTaskSysService projectTaskSysService;

    /**
     * 新建或者修改任务
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新建或者修改系统任务")
    @PostMapping("/saveOrUpdate")
    //   @RequestPermissions("plm:sys:task:saveOrUpdate")
    public ApiResult<Object> saveOrUpdate(@RequestBody @Validated SysTaskDTO dto) {
        Boolean result = projectTaskSysService.saveOrUpdateSysTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 系统任务-编辑任务-获取任务详情
     *
     * @param taskId
     * @return
     */
    @LogViewService
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
    @LogAction(value = LogActionEnum.DELETE, desc = "删除系统任务")
    @PostMapping("/remove")
    //  @RequestPermissions("plm:sys:task:remove")
    public ApiResult<Object> paging(String taskId) {
        Boolean flag=projectTaskSysService.removeTask(taskId);
        return flag==true?success():failure();
    }

    /**
     * 立项模板 新建任务 获取前置任务列表【优化3】
     */
    @GetMapping("/list")
    //  @RequestPermissions("plm:sys:task:list")
    public ApiResult<Object> list(@Param("templateId") String templateId) {
        List<Map<String,Object>> list= projectTaskSysService.taskList(templateId);
        return success(list);
    }

}

