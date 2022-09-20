package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProjectMemberDTO;
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

    void add(String productId,String projectId, List<ProjectMemberDTO> members);

    void saveMember(String flagId, String productId);

    void saveMemberByProject(String productId,String projectId, String flagId);

    void saveMemberByTemplate(String productId,String projectId, String flagId);
}
