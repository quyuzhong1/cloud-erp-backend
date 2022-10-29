package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.DocsNameDTO;
import com.erp.model.plm.entity.TaskDocsNameEntity;

import java.util.List;

/**
 * @Classname TaskDocsNameService
 * @Description TODO
 * @Date 2022-09-22 12:21
 * @Created by yl
 */
public interface TaskDocsNameService  extends IService<TaskDocsNameEntity> {
    Boolean saveDocsName(DocsNameDTO dto);
    List<DocsDTO> getDocsNameList(String productId);

    List<TaskDocsNameEntity> getDocsNameByProductId(String productId);

    List<TaskDocsNameEntity> saveBySysTaskIds(List<String> sysTaskIds,String productId);

    List<TaskDocsNameEntity> saveBySys(String productId);
}
