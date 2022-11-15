package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.TemplateRoleDTO;
import com.erp.server.plm.service.TemplateMembersService;
import com.erp.server.plm.service.TemplateRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板角色成员
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
     * 角色成员列表查询
     *
     * @author Will
     * @date: 2022/11/15 13:58
     * @param dto
     * @return ApiResult<PagingVO<List<TemplateRoleDTO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<TemplateRoleDTO>>> paging(@RequestBody PagingDTO<TemplateRoleDTO> dto) {
        PagingVO<List<TemplateRoleDTO>> pagingVO = templateRoleService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增角色
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
     * 新增成员
     *
     * @author Will
     * @date: 2022/11/15 15:04
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/saveTemplateMembers")
    public ApiResult saveTemplateMembers(@RequestBody @Validated TemplateRoleDTO dto) {
        Boolean flag = templateMembersService.saveTemplateMembers(dto);
        return flag ? success() : failure();
    }

    /**
     * 编辑成员
     *
     * @author Will
     * @date: 2022/11/15 15:05
     * @param dto
     * @return ApiResult
     */
    @PutMapping("/updateTemplateMembers")
    public ApiResult updateTemplateMembers(@RequestBody @Validated TemplateRoleDTO dto) {
        Boolean flag = templateMembersService.updateTemplateMembers(dto);
        return flag ? success() : failure();
    }

    /**
     * 删除成员
     *
     * @author Will
     * @date: 2022/11/15 15:57
     * @param dto
     * @return ApiResult
     */
    @DeleteMapping("/deleteTemplateMembers")
    public ApiResult deleteTemplateMembers(@RequestBody @Validated TemplateRoleDTO dto) {
        Boolean flag = templateMembersService.deleteTemplateMembers(dto);
        return flag ? success() : failure();
    }


}
