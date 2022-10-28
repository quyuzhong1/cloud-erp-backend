package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
import com.erp.model.plm.entity.TemplateRoleRefMembersEntity;

import java.util.List;


/**
 *
 */
public interface TemplateRoleRefMembersService extends IService<TemplateRoleRefMembersEntity> {

    void saveRoleRefMembers(String templateId, String productId);

    void copyTemplateRoleRefMembers(String flagId, String productId, String projectId, List<TemplateCopySourceDTO> copyRoleSourceList);
}
