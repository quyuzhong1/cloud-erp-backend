package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.MemberPagingShowDTO;
import com.erp.model.plm.dto.RemoveProjectMemberDTO;
import com.erp.model.plm.dto.SaveOrUpdateProjectMemberDTO;
import com.erp.model.plm.dto.MemberPagingDTO;
import com.erp.server.plm.service.ProjectMembersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;

/** 产品开发管理
 * <p>
 * 项目成员表 前端控制器
 * </p>
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
    public ApiResult save(@RequestBody @Validated SaveOrUpdateProjectMemberDTO dto) {
        Boolean flag = projectMembersService.saveOrUpdateMember(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 移除成员
     * @param
     * @return
     */
    @PostMapping("/remove")
    public ApiResult rmove(@RequestBody @Validated RemoveProjectMemberDTO dto) {
        Boolean flag = projectMembersService.removeMembers(dto);
        return flag == true ? success() : failure();
    }
}

