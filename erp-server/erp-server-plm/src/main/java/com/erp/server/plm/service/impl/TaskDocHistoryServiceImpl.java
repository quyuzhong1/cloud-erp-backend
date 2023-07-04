package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.plm.dto.DocHistoryDTO;
import com.erp.model.plm.entity.TaskDocHistoryEntity;
import com.erp.model.plm.entity.TaskDocsFinishEntity;
import com.erp.server.plm.mapper.TaskDocHistoryMapper;
import com.erp.server.plm.service.TaskDocHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 任务文档历史表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@Slf4j
@Service
public class TaskDocHistoryServiceImpl extends SuperServiceImpl<TaskDocHistoryMapper, TaskDocHistoryEntity> implements TaskDocHistoryService {


    /**
     * 添加历史文档
     *
     * @param oldDocs
     * @return void
     * @author yl
     * @date 2023-06-25 9:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addHistory(TaskDocsFinishEntity oldDocs) {
        TaskDocHistoryEntity entity = new TaskDocHistoryEntity();
        entity.setTaskId(oldDocs.getTaskId());
        entity.setFileName(oldDocs.getFileName());
        entity.setFileSize(oldDocs.getFileSize());
        entity.setFileSuffix(oldDocs.getFileSuffix());
        entity.setFileType(oldDocs.getFileType());
        entity.setFileUrl(oldDocs.getFileUrl());
        entity.setFinishDocId(oldDocs.getId());
        entity.setProductId(oldDocs.getProductId());
        entity.setUploadType(oldDocs.getUploadType());
        entity.setRequireDocId(oldDocs.getTaskDocsId());
        Integer maxVersion = getMaxVersion(oldDocs.getId());
        entity.setChangeVersion(maxVersion);
        this.save(entity);

    }

    /**
     * 获取版本
     *
     * @param finishDocId
     * @return
     */
    public Integer getMaxVersion(String finishDocId) {
        QueryWrapper<TaskDocHistoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("MAX(change_version) as maxVersion");
        queryWrapper.lambda().eq(TaskDocHistoryEntity::getFinishDocId, finishDocId);
        List<Object>  list = baseMapper.selectObjs(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            Object obj = list.get(0);
            if (obj != null) {
                return (Integer) obj + 1;
            } else {
                return 1;
            }

        }
        return 1;

    }

    /**
     * 获取文档历史记录
     *
     * @param finishDocsId
     * @return java.util.List<com.erp.model.plm.dto.DocHistoryDTO.InfoDTO>
     * @author yl
     * @date 2023-06-25 10:00
     */
    @Override
    public List<DocHistoryDTO.InfoDTO> historyList(String finishDocsId) {

        return baseMapper.historyList(finishDocsId);
    }


    /**
     * 任务审核通过
     *
     * @param taskId
     * @return void
     * @author yl
     * @date 2023-06-25 10:59
     */
    @Override
    public void updateChangeResult(String taskId) {
        this.lambdaUpdate().set(TaskDocHistoryEntity::getIsChangeSuccess, Boolean.TRUE).
                eq(TaskDocHistoryEntity::getTaskId, taskId).
                eq(TaskDocHistoryEntity::getIsChangeSuccess, Boolean.FALSE).update();
    }


    /**
     * 根据任务ids 更改文档历史
     * @author yl
     * @date 2023-07-04 16:58
     * @param isChangeDocsTaskIdList
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChangeResultByTaskIds(List<String> isChangeDocsTaskIdList) {
        if(CollectionUtils.isNotEmpty(isChangeDocsTaskIdList)){
            this.lambdaUpdate().set(TaskDocHistoryEntity::getIsChangeSuccess, Boolean.TRUE).
                    in(TaskDocHistoryEntity::getTaskId, isChangeDocsTaskIdList).
                    eq(TaskDocHistoryEntity::getIsChangeSuccess, Boolean.FALSE).update();
        }

    }


}
