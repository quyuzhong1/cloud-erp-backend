package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.TemplateRoleEntity;


/**
 *
 */
public interface TemplateRoleService extends IService<TemplateRoleEntity> {

    void saveTemplateRole(String templateId, String productId);
}
