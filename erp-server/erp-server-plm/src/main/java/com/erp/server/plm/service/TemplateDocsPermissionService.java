package com.erp.server.plm.service;




import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.DocsPermissionEntity;
import com.erp.model.plm.entity.TemplateDocsPermissionEntity;

import java.util.List;

/**
*
*/
public interface TemplateDocsPermissionService extends IService<TemplateDocsPermissionEntity> {


    void saveTemplateDocsPermission(String templateId, String productId);

    void copyTemplateDeliveryDocs(String flagId, String productId, List<CopySourceDTO> taskSourceList, List<CopySourceDTO> deliveryDocsSourceList);
}
