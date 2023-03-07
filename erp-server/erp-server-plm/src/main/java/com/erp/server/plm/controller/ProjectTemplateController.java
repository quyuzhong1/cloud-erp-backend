package com.erp.server.plm.controller;


import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.enums.ChargeSuperiorEnum;
import com.erp.model.plm.vo.DropdownEnumVO;
import com.erp.model.plm.vo.PreTaskListVO;
import com.erp.server.plm.service.ProjectTemplateService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/template")
public class ProjectTemplateController extends BaseController {

    @Autowired
    private ProjectTemplateService projectTemplateService;

    /**
     * 模板管理-列表分页查询
     *
     * @param dto
     * @return ApiResult<PagingVO < ProjectTemplateDTO>>
     * @author Will
     * @date: 2022/11/11 14:53
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<ProjectTemplateDTO>> paging(@RequestBody PagingDTO<BaseSearchDTO> dto) {
        PagingVO<ProjectTemplateDTO> pagingVO = projectTemplateService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 模板管理-新增或修改
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2022/11/11 15:33
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated ProjectTemplateSaveOrUpdateDTO dto) {
        Boolean flag = projectTemplateService.saveOrUpdate(dto);
        return flag ? success() : failure();
    }

    /**
     * 模板管理-修改状态
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2022/11/11 15:33
     */
    @PutMapping("/updateStatus")
    public ApiResult updateTemplateStatus(@RequestBody @Validated ProjectTemplateUpdateStatusDTO dto) {
        Boolean flag = projectTemplateService.updateTemplateStatus(dto);
        return flag ? success() : failure();
    }

    /**
     * 模板管理-查询模板角色
     *
     * @param templateId
     * @return ApiResult
     * @author Will
     * @date: 2023/1/9 15:39
     */
    @GetMapping("/listTemplateRole")
    public ApiResult<List<SysRoleDTO>> listTemplateRole(@Param("templateId") String templateId) {
        List<SysRoleDTO> list = projectTemplateService.listTemplateRole(templateId);
        return success(list);
    }


    /**
     * 模板管理-查询上级负责人
     *
     * @return ApiResult
     * @author Will
     * @date: 2023/1/9 10:29
     */
    @GetMapping("/listSuperior")
    public ApiResult<List<DropdownEnumVO>> listSuperior() {
        List<DropdownEnumVO> result = Arrays.stream(ChargeSuperiorEnum.values())
                .map(x -> new DropdownEnumVO(x.getCode(), x.getName(), x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 获取模板的产品属性
     *
     * @return
     */
    @GetMapping("/getProductPropertyList")
    public ApiResult getProductPropertyList() {
        List<Map<String, Object>> list = projectTemplateService.getProductPropertyList();
        return success(list);
    }

    /**
     * 更新前置任务列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/update/pre/task")
    public ApiResult setPreTask(@RequestBody @Validated @NotEmpty(message = "参数列表不能为空") PreTemplateTaskUpdateDTO dto) {
        Boolean flag = projectTemplateService.updatePreTask(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询前置任务列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/list/pre/task")
    public ApiResult<List<PreTaskListVO>> listPreTask(@RequestBody @Validated TemplatePreTaskDTO dto) {
        List<PreTaskListVO> reusltList = projectTemplateService.ListPreTaskByTaskId(dto);
        return success(reusltList);
    }


    /**
     * 根据产品属性获取到对应模板【优化4】
     * @param propertyId
     * @return
     */
    @GetMapping("/getTemplateByProperty")
    public ApiResult<List<Map<String,Object>>> getByPropertyId(@Param("propertyId") String propertyId) {
        List<Map<String,Object>> templateList = projectTemplateService.getByPropertyId(propertyId);
        return success(templateList);

    }

}

