package com.erp.server.plm.service;




import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.DocsPermissionEntity;

import java.util.List;

/**
*
*/
public interface DocsPermissionService extends IService<DocsPermissionEntity> {

    List<String> getDocsIdsByUserId(String uid);
}
