package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.vo.ItemMemberVO;
import org.apache.ibatis.annotations.Param;

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


    void saveMemberByProject(String productId,String projectId, String flagId);


    List<ProjectMembersEntity> getListByProductId(String productId);

    Boolean saveOrUpdateMember(SaveOrUpdateProjectMemberDTO dto);

    PagingVO<List<MemberPagingShowDTO>> paging(PagingDTO<MemberPagingDTO> dto);

    /**
     * 根据产品表id 集合
     * 和 项目成员表id 集合获取对应数据
     * @param memberList 项目成员表id 集合
     * @param productIdList 产品表id 集合
     * @return
     */
    List<ProductRoleMemberDTO> getProductCountByMemberList(List<String> memberList,List<String> productIdList);



    Boolean removeMembers(RemoveProjectMemberDTO dto);

    List<ProjectMembersEntity> getChargeList(String  productId);

    List<ProjectMemberDTO> memberList(String productId);

    Boolean ifProjectMember(String userId, String productId);

    List<TaskConductDTO> getUserTaskConduct(List<FindUserDTO> userList, List<Integer> stateList);

    void addRoleAndMembersByApproval(String productId,String productPropertyId);

    List<ProjectMembersEntity> getByMemberIds(List<String> memberIds,String productId);

    List<ProjectMembersEntity> listByRoleIds(List<String> roleIdList, String productId);

   Boolean saveByRoleAndMembers(String productId,String projectId,String roleName,List<String> memberList);

    List<ItemMemberVO> getByProductIds(List<String> productIds);

    List<MemberPagingShowDTO> listByRoleNames(List<String> roles, String templateId, String roleName);

    List<MemberPagingShowDTO> listByMembers(String productId);
}
