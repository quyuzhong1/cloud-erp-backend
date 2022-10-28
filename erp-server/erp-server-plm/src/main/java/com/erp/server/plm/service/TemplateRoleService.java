package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
import com.erp.model.plm.entity.TemplateRoleEntity;

import java.util.List;


/**
 *
 */
public interface TemplateRoleService extends IService<TemplateRoleEntity> {

    void saveTemplateRole(String templateId, String productId);

    List<TemplateCopySourceDTO> copyTemplateRole(String flagId, String productId, String projectId);
}
