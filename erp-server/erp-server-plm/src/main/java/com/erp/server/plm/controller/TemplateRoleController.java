package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.TemplateRoleEntity;
import com.erp.server.plm.service.TemplateMembersService;
import com.erp.server.plm.service.TemplateRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板管理
 *
 * @author Will
 * @version 1.0
 * @description:
 * @date 2022/11/15 10:59
 */
@RestController
@RequestMapping("/plm/templateRole")
public class TemplateRoleController extends BaseController {

    @Autowired
    private TemplateRoleService templateRoleService;

    @Autowired
    private TemplateMembersService templateMembersService;

    /**
     * 模板详情-角色成员-列表分页查询
     *
     * @author Will
     * @date: 2022/11/15 13:58
     * @param dto
     * @return ApiResult<PagingVO<List<TemplateRoleShowDTO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<TemplateRoleShowDTO>>> paging(@RequestBody PagingDTO<TemplateSearchDTO> dto) {
        PagingVO<List<TemplateRoleShowDTO>> pagingVO = templateRoleService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 模板详情-角色成员-查询所有角色
     *
     * @author Will
     * @date: 2022/11/16 12:04
     * @param templateId
     * @return ApiResult<List<TemplateRoleEntity>>
     */
    @GetMapping("/getAllRoles")
    public ApiResult<List<TemplateRoleEntity>> getAllRoles(@RequestParam("templateId") String templateId) {
        List<TemplateRoleEntity> list = templateRoleService.getAllRoles(templateId);
        return success(list);
    }

    /**
     * 模板详情-角色成员-新增角色
     *
     * @author Will
     * @date: 2022/11/15 14:00
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/saveTemplateRole")
    public ApiResult saveTemplateRole(@RequestBody @Validated TemplateRoleDTO dto) {
        Boolean flag = templateRoleService.saveTemplateRole(dto);
        return flag ? success() : failure();
    }

    /**
     * 模板详情-角色成员-新增成员
     *
     * @author Will
     * @date: 2022/11/15 15:04
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/saveTemplateMembers")
    public ApiResult saveTemplateMembers(@RequestBody @Validated TemplateMembersAddOrUpdateDTO dto) {
        Boolean flag = templateMembersService.saveTemplateMembers(dto);
        return flag ? success() : failure();
    }

    /**
     * 模板详情-角色成员-删除成员
     *
     * @author Will
     * @date: 2022/11/15 15:57
     * @param dto
     * @return ApiResult
     */
    @DeleteMapping("/deleteTemplateMembers")
    public ApiResult deleteTemplateMembers(@RequestBody @Validated TemplateRoleMembersDeleteDTO dto) {
        Boolean flag = templateMembersService.deleteTemplateMembers(dto);
        return flag ? success() : failure();
    }


}
