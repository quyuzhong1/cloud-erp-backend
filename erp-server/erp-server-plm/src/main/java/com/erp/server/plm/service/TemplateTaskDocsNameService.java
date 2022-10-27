package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.TemplateTaskDocsNameEntity;



/**
 *
 */
public interface TemplateTaskDocsNameService extends IService<TemplateTaskDocsNameEntity> {

    void saveTemplateDocsName(String templateId, String productId);
}
