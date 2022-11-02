package com.erp.server.plm.service;




import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.DocsPermissionEntity;

import java.util.List;

/**
*
*/
public interface DocsPermissionService extends IService<DocsPermissionEntity> {

    List<String> getDocsIdsByUserId(String uid,String productId);

    void removePermission(String taskId);

    void removeByDeliveryDocsId(List<String> docsIds);

    List<DocsPermissionEntity> getDocsPermissionByProductId(String productId);

    List<String> getDocsIdsByProductId(String flagId);

    List<String> getDocsIdsByRoleIds(List<String> userRoleIds,String productId);

    List<String> getAllDeliveryDocsIds(String productId);
}
