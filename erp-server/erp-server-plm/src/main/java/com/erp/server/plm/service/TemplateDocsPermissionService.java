package com.erp.server.plm.service;




import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.TemplateDocsPermissionEntity;

import java.util.List;

/**
*
*/
public interface TemplateDocsPermissionService extends IService<TemplateDocsPermissionEntity> {


    void saveTemplateDocsPermission(String templateId, String productId, List<CopySourceDTO> sourceDeliveryList,List<CopySourceDTO> taskSourceList,List<CopySourceDTO> sourceRoleList);

    void copyTemplateDeliveryDocs(String flagId, String productId, List<CopySourceDTO> taskSourceList, List<CopySourceDTO> deliveryDocsSourceList);
    /**
     * @description: 根据角色ids和模板id查询
     * @author Will
     * @date: 2022/11/14 18:18
     * @param userRoleIds
     * @param templateId
     * @return List<String>
     */
    List<String> getDocsIdsByRoleIds(List<String> userRoleIds, String templateId);
    /**
     * @description: 根据模板id查询所有文档ids
     * @author Will
     * @date: 2022/11/14 18:30
     * @param templateId
     * @return List<String>
     */
    List<String> getAllDeliveryDocsIds(String templateId);
}
