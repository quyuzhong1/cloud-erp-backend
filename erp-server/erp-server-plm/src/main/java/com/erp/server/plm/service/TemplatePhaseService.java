package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.TemplatePhaseEntity;

import java.util.List;


/**
 *
 */
public interface TemplatePhaseService extends IService<TemplatePhaseEntity> {

    void saveTemplatePhase(String templateId, String productId);

    List<CopySourceDTO> copyTemplatePhase(String flagId, String productId, String projectId);
}
