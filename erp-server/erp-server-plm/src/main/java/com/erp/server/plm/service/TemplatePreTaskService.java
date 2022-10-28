package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
import com.erp.model.plm.entity.TemplatePreTaskEntity;

import java.util.List;

/**
 *
 */
public interface TemplatePreTaskService extends IService<TemplatePreTaskEntity> {

    void saveTemplatePreTask(String templateId, String productId);

    void copyTemplatePreTask(String flagId, String productId, List<TemplateCopySourceDTO> taskSourceList);
}
