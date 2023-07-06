package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.plm.dto.DocHistoryDTO;
import com.erp.model.plm.entity.TaskDocHistoryEntity;
import com.erp.model.plm.entity.TaskDocsFinishEntity;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.TaskDocHistoryMapper;
import com.erp.server.plm.service.TaskDocHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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
     * @param taskDocs
     * @return void
     * @author yl
     * @date 2023-06-25 9:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addHistory(TaskDocsFinishEntity taskDocs) {
        String finishDocId = taskDocs.getId();
        List<TaskDocHistoryEntity> list = this.listByFinishDocId(finishDocId);
        if (CollectionUtils.isNotEmpty(list)) {
            TaskDocHistoryEntity one = list.get(0);
            if (one.getFileUrl().equals(taskDocs.getFileUrl())) {
                return;
            }

        }
        TaskDocHistoryEntity entity = new TaskDocHistoryEntity();

        entity.setFileName(taskDocs.getFileName());
        entity.setTaskId(taskDocs.getTaskId());

        entity.setFileSize(taskDocs.getFileSize());
        entity.setFileSuffix(taskDocs.getFileSuffix());
        entity.setFileType(taskDocs.getFileType());
        entity.setFileUrl(taskDocs.getFileUrl());
        entity.setFinishDocId(taskDocs.getId());
        entity.setProductId(taskDocs.getProductId());
        entity.setUploadType(taskDocs.getUploadType());
        entity.setRequireDocId(taskDocs.getTaskDocsId());
        Integer maxVersion = getMaxVersion(taskDocs.getId());
        entity.setChangeVersion(maxVersion);
        this.save(entity);

    }


    /**
     * 方法说明
     *
     * @param finishDocId
     * @return java.util.List<com.erp.model.plm.entity.TaskDocsFinishEntity>
     * @author yl
     * @date 2023-07-06 16:53
     */
    private List<TaskDocHistoryEntity> listByFinishDocId(String finishDocId) {


        return this.lambdaQuery().eq(TaskDocHistoryEntity::getFinishDocId, finishDocId).orderByDesc(TaskDocHistoryEntity::getChangeVersion).list();
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
        List<Object> list = baseMapper.selectObjs(queryWrapper);
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
     *
     * @param isChangeDocsTaskIdList
     * @return void
     * @author yl
     * @date 2023-07-04 16:58
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChangeResultByTaskIds(List<String> isChangeDocsTaskIdList) {
        if (CollectionUtils.isNotEmpty(isChangeDocsTaskIdList)) {
            this.lambdaUpdate().set(TaskDocHistoryEntity::getIsChangeSuccess, Boolean.TRUE).
                    in(TaskDocHistoryEntity::getTaskId, isChangeDocsTaskIdList).
                    eq(TaskDocHistoryEntity::getIsChangeSuccess, Boolean.FALSE).update();
        }

    }

    /**
     * 根据任务ids 查询历史文档
     *
     * @param taskIdList
     * @return java.util.List<com.erp.model.plm.entity.TaskDocHistoryEntity>
     * @author yl
     * @date 2023-07-04 20:20
     */
    @Override
    public List<TaskDocHistoryEntity> listByTaskIdList(List<String> taskIdList) {
        if (CollectionUtils.isEmpty(taskIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(TaskDocHistoryEntity::getTaskId, taskIdList).orderByDesc(TaskDocHistoryEntity::getCreateTime).list();
    }


    /**
     * 批量提交
     *
     * @param resultList
     * @return void
     * @author yl
     * @date 2023-07-05 19:50
     */
    @Override
    public void addBatchHistory(List<TaskDocsFinishEntity> resultList) {
        if (CollectionUtils.isNotEmpty(resultList)) {
            List<TaskDocHistoryEntity> addList = new ArrayList<>(resultList.size());
            for (TaskDocsFinishEntity item : resultList) {
                TaskDocHistoryEntity entity = new TaskDocHistoryEntity();
                Integer uploadType = item.getUploadType();
                if (!TaskConstant.LOCAL_UPLOAD.equals(uploadType)) {
                    entity.setFileName(item.getFileUrl());
                } else {
                    entity.setFileName(item.getFileName());
                }
                entity.setTaskId(item.getTaskId());
                entity.setFileSize(item.getFileSize());
                entity.setFileSuffix(item.getFileSuffix());
                entity.setFileType(item.getFileType());
                entity.setFileUrl(item.getFileUrl());
                entity.setFinishDocId(item.getId());
                entity.setProductId(item.getProductId());
                entity.setUploadType(item.getUploadType());
                entity.setRequireDocId(item.getTaskDocsId());
                Integer maxVersion = getMaxVersion(item.getId());
                entity.setChangeVersion(maxVersion);
                addList.add(entity);

            }
            this.saveBatch(addList);

        }

    }


}
