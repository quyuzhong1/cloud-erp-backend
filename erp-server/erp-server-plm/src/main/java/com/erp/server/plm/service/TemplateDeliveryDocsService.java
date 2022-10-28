package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;

import java.util.List;


/**
 *
 */
public interface TemplateDeliveryDocsService extends IService<TemplateDeliveryDocsEntity> {

    void saveTemplateDeliveryDocs(String templateId, String productId);

    void copyTemplateDeliveryDocs(String templateId, String productId, List<TemplateCopySourceDTO> taskSourceList, List<TemplateCopySourceDTO> docsNameSourceList);
}
