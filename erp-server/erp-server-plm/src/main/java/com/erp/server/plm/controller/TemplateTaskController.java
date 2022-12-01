package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.TemplateSearchDTO;
import com.erp.model.plm.dto.TemplateTaskDTO;
import com.erp.model.plm.dto.TemplateTaskParamDTO;
import com.erp.model.plm.dto.TemplateTaskShowDTO;
import com.erp.server.plm.service.TemplateTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模板管理
 *
 * @author Will
 * @version 1.0
 * @description:
 * @date 2022/11/14 9:15
 */
@RestController
@RequestMapping("/plm/template/task")
public class TemplateTaskController extends BaseController {

    @Autowired
    private TemplateTaskService templateTaskService;


    /**
     * 模板详情-模板任务-列表分页查询
     *
     * @author Will
     * @date: 2022/11/14 9:25
     * @param dto
     * @return ApiResult<PagingVO<TemplateTaskShowDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<TemplateTaskShowDTO>> paging(@RequestBody PagingDTO<TemplateSearchDTO> dto) {
        PagingVO<TemplateTaskShowDTO> pagingVO = templateTaskService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 模板详情-模板任务-新增或修改
     *
     * @author Will
     * @date: 2022/11/14 9:25
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated TemplateTaskDTO dto) {
        Boolean flag = templateTaskService.saveOrUpdate(dto);
        return flag ? success() : failure();
    }

    /**
     * 模板详情-模板任务-删除
     *
     * @author Will
     * @date: 2022/11/14 14:58
     * @param dto
     * @return ApiResult
     */
    @DeleteMapping("/remove")
    public ApiResult remove(@RequestBody @Validated TemplateTaskParamDTO dto) {
        Boolean flag = templateTaskService.removeTask(dto.getId(),dto.getTemplateId());
        return flag == true ? success() : failure();
    }

    /**
     * 模板详情-模板任务-获取前置任务列表
     *
     * @author Will
     * @date: 2022/11/18 14:58
     * @param templateId
     * @return ApiResult
     */
    @GetMapping("/list")
    public ApiResult list(@RequestParam("templateId") String templateId) {
        List<Map<String, Object>> list = templateTaskService.getTaskListByTemplateId(templateId);
        return success(list);
    }

    /**
     * 模板详情-模板任务-任务详情数据
     *
     * @author Will
     * @date: 2022/11/18 14:32
     * @param dto
     * @return ApiResult<TemplateTaskDTO>
     */
    @PostMapping("/taskDetails")
    public ApiResult<TemplateTaskDTO> taskDetails(@RequestBody @Validated TemplateTaskParamDTO dto) {
        TemplateTaskDTO taskDTO = templateTaskService.taskDetails(dto);
        return success(taskDTO);
    }

}
