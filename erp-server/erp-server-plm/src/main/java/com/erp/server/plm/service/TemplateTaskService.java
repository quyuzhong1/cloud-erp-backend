package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.TemplateTaskEntity;

/**
 * @Classname TemplateTaskService
 * @Description TODO
 * @Date 2022-09-20 15:34
 * @Created by yl
 */
public interface TemplateTaskService extends IService<TemplateTaskEntity> {
    void saveTemplateTask(String templateId, String productId);
}
