package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.RoleRefMemberDTO;
import com.erp.model.plm.entity.RoleRefMemberEntity;

import java.util.List;


/**
 *
 */
public interface RoleRefMemberService extends IService<RoleRefMemberEntity> {

    List<RoleRefMemberDTO> getByRoleIds(List<String> roleIds);

    void saveRef(List<String>  membersTableIds, String roleId,String productId);

    void checkRoleMember(String id,String roleId,List<String> memberIds,String productId);

    List<RoleRefMemberEntity> getByProductId(String productId);

    List<String> getUserRole(String userId,String productId);

    List<RoleRefMemberEntity> listByMembersIds(List<String> membersIds);

    List<RoleRefMemberEntity> getByRoleIdsAndProductId(List<String> roleIdList, String productId);
}
