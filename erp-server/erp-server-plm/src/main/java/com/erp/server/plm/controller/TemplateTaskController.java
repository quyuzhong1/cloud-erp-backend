package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.TemplateSearchDTO;
import com.erp.model.plm.dto.TemplateTaskDTO;
import com.erp.model.plm.dto.TemplateTaskDeleteDTO;
import com.erp.model.plm.dto.TemplateTaskShowDTO;
import com.erp.server.plm.service.TemplateTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * 模板任务列表查询
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
     * 模板任务新增或修改
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
     * 删除模板任务
     *
     * @author Will
     * @date: 2022/11/14 14:58
     * @param dto
     * @return ApiResult
     */
    @DeleteMapping("/remove")
    public ApiResult remove(@RequestBody @Validated TemplateTaskDeleteDTO dto) {
        Boolean flag = templateTaskService.removeTask(dto.getId(),dto.getTemplateId());
        return flag == true ? success() : failure();
    }



}
