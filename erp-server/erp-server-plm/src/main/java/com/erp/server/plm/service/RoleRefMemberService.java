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

    void saveOrUpdateRef(String roleRefMemberId, String id, String roleId);
}
