package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.DocsChangeRecordEntity;

import java.util.List;


/**
 *
 */
public interface DocsChangeRecordService extends IService<DocsChangeRecordEntity> {

    void addRecord(String content, String taskId, String finishDocsId, String processId);

    List<DocsChangeRecordEntity> listByTaskId(String taskId);
}
