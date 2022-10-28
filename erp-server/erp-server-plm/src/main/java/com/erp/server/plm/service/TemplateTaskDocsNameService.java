package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
import com.erp.model.plm.entity.TemplateTaskDocsNameEntity;

import java.util.List;


/**
 *
 */
public interface TemplateTaskDocsNameService extends IService<TemplateTaskDocsNameEntity> {

    void saveTemplateDocsName(String templateId, String productId);

    List<TemplateCopySourceDTO> copyTemplateDocsName(String flagId, String productId, String projectId);
}
