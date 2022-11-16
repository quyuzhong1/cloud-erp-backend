package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.TemplatePreTaskEntity;

import java.util.List;

/**
 *
 */
public interface TemplatePreTaskService extends IService<TemplatePreTaskEntity> {

    void saveTemplatePreTask(String templateId, String productId);

    void copyTemplatePreTask(String flagId, String productId, List<CopySourceDTO> taskSourceList);
    /**
     * @description: 保存前置任务
     * @author Will
     * @date: 2022/11/16 10:26
     * @param taskId
     * @param preTaskIdList
     * @param templateId

     */
    void saveTemplatePreTaskList(String taskId, List<String> preTaskIdList, String templateId);
}
