package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectMembersEntity;

import java.util.List;

/**
 * <p>
 * 项目成员表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectMembersService extends IService<ProjectMembersEntity> {

    void add(String productId,String projectId, List<String> members);

    void saveMember(String flagId, String productId);

    void saveMemberByProject(String productId,String projectId, String flagId);

    void saveMemberByTemplate(String productId,String projectId, String flagId);

    List<ProjectMembersEntity> getListByProductId(String productId);

    Boolean saveOrUpdateMember(SaveOrUpdateProjectMemberDTO dto);

    PagingVO<List<MemberPagingShowDTO>> paging(PagingDTO<MemberPagingDTO> dto);

    List<ProductRoleMemberDTO> getProductCountByMemberList(List<String> memberList);



    Boolean removeMembers(RemoveProjectMemberDTO dto);
}
