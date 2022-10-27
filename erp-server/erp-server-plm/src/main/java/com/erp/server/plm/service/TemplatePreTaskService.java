package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.TemplatePreTaskEntity;

/**
 *
 */
public interface TemplatePreTaskService extends IService<TemplatePreTaskEntity> {

    void saveTemplatePreTask(String templateId, String productId);
}
