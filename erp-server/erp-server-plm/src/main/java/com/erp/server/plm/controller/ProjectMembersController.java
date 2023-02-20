package com.erp.server.plm.controller;


import com.common.core.utils.MathUtil;
import com.common.core.controller.vo.ApiResult;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.server.plm.service.ProjectMembersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import java.util.List;

/** 产品开发管理

 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/project/member")
public class ProjectMembersController extends BaseController {

    @Autowired
    private ProjectMembersService projectMembersService;



    /**
     * 设置-项目成员分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    // @RequestPermissions("plm:project:member:paging")
    public ApiResult<PagingVO<List<MemberPagingShowDTO>>> paging(@RequestBody @Validated PagingDTO<MemberPagingDTO> dto) {
        PagingVO<List<MemberPagingShowDTO>> pagingVO = projectMembersService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 保存或者修改项目成员
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    //@RequestPermissions("plm:project:member:saveOrUpdate")
    public ApiResult save(@RequestBody @Validated SaveOrUpdateProjectMemberDTO dto) {
        dto.setFlag(MathUtil.ONE);
        Boolean flag = projectMembersService.saveOrUpdateMember(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 移除成员
     * @param
     * @return
     */
    @PostMapping("/remove")
    //  @RequestPermissions("plm:project:member:remove")
    public ApiResult remove(@RequestBody @Validated RemoveProjectMemberDTO dto) {
        Boolean flag = projectMembersService.removeMembers(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 新建任务-获取项目负责人列表
     * @param
     * @return
     */
    @GetMapping("/list")
    //@RequestPermissions("plm:project:member:list")
    public ApiResult<List<ProjectMemberDTO>> list(String productId) {
        List<ProjectMemberDTO> resultList = projectMembersService.memberList(productId);
        return  success(resultList);
    }
}

