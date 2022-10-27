package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.TemplatePhaseEntity;



/**
 *
 */
public interface TemplatePhaseService extends IService<TemplatePhaseEntity> {

    void saveTemplatePhase(String templateId, String productId);
}
