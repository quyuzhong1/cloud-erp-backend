package com.erp.server.plm.controller;


import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProjectTemplateDTO;
import com.erp.model.plm.dto.ProjectTemplateSaveOrUpdateDTO;
import com.erp.model.plm.dto.ProjectTemplateUpdateStatusDTO;
import com.erp.model.plm.dto.SysRoleDTO;
import com.erp.model.sys.dto.UserDTO;
import com.erp.server.plm.service.ProjectTemplateService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @author Will
     * @date: 2022/11/11 14:53
     * @param dto
     * @return ApiResult<PagingVO<ProjectTemplateDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<ProjectTemplateDTO>> paging(@RequestBody  PagingDTO<BaseSearchDTO> dto) {
        PagingVO<ProjectTemplateDTO> pagingVO = projectTemplateService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 模板管理-新增或修改
     *
     * @author Will
     * @date: 2022/11/11 15:33
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated ProjectTemplateSaveOrUpdateDTO dto) {
        Boolean flag = projectTemplateService.saveOrUpdate(dto);
        return flag ? success() : failure();
    }

    /**
     * 模板管理-修改状态
     *
     * @author Will
     * @date: 2022/11/11 15:33
     * @param dto
     * @return ApiResult
     */
    @PutMapping("/updateStatus")
    public ApiResult updateTemplateStatus(@RequestBody @Validated ProjectTemplateUpdateStatusDTO dto) {
        Boolean flag = projectTemplateService.updateTemplateStatus(dto);
        return flag ? success() : failure();
    }

    /**
     * 模板管理-查询模板角色
     * @author Will
     * @date: 2023/1/9 15:39
     * @param templateId
     * @return ApiResult
     */
    @GetMapping("/listTemplateRole")
    public ApiResult<List<SysRoleDTO>> listTemplateRole(@Param("templateId") String templateId) {
        List<SysRoleDTO> list =  projectTemplateService.listTemplateRole(templateId);
        return success(list);
    }

    /**
     * 模板管理-查询上级负责人（负责人类型type,0角色，1人员）
     * @author Will
     * @date: 2023/1/9 10:29
     * @param id
     * @param type
     * @return ApiResult
     */
    @GetMapping("/listSuperior")
    public ApiResult<List<UserDTO>> listSuperior(@Param("id") String id,@Param("type") Integer type) {
        List<UserDTO> list =  projectTemplateService.listSuperior(id,type);
        return success(list);
    }


}

