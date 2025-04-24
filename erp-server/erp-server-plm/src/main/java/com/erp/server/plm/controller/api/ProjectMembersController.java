package com.erp.server.plm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.MathUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
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
@LogSystemModule("产品开发管理")
@RequestMapping("project/member")
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
    public ApiResult<PagingVO<MemberPagingShowDTO>> paging(@RequestBody @Validated PagingDTO<MemberPagingDTO> dto) {
        PagingVO<MemberPagingShowDTO> pagingVO = projectMembersService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 保存或者修改项目成员
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "保存或者修改项目成员")
    @PostMapping("/saveOrUpdate")
    //@RequestPermissions("plm:project:member:saveOrUpdate")
    public ApiResult<Object> save(@RequestBody @Validated SaveOrUpdateProjectMemberDTO dto) {
        dto.setFlag(MathUtil.ONE);
        Boolean flag = projectMembersService.saveOrUpdateMember(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 移除成员
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "移除成员")
    @PostMapping("/remove")
    //  @RequestPermissions("plm:project:member:remove")
    public ApiResult<Object> remove(@RequestBody @Validated RemoveProjectMemberDTO dto) {
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

