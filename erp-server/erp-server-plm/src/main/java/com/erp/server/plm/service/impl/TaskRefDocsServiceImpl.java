package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.finishDocsDTO;
import com.erp.model.plm.entity.TaskRefDocsEntity;
import com.erp.server.plm.mapper.TaskRefDocsMapper;
import com.erp.server.plm.service.TaskRefDocsService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * 任务文档关系表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class TaskRefDocsServiceImpl extends ServiceImpl<TaskRefDocsMapper, TaskRefDocsEntity> implements TaskRefDocsService {

    //保存任务 和文档的对应关系
    @Override
    public void batchRef(String taskId, List<finishDocsDTO> docsList) {
        //先根据任务id 删除 关系
        removeByTaskId(taskId);
        List<TaskRefDocsEntity> saveList = new LinkedList<>();
        for (finishDocsDTO item : docsList) {
            TaskRefDocsEntity entity = new TaskRefDocsEntity();
            entity.setDocsId(item.getDocsId());
            entity.setTaskId(taskId);
            saveList.add(entity);
        }
        this.saveBatch(saveList);

    }

    /**
     * 根据任务id 删除 对应关系表
     *
     * @param taskId
     * @return void
     * @author yl
     * @date 2022-09-15 17:36
     */
    private void removeByTaskId(String taskId) {
        LambdaQueryWrapper<TaskRefDocsEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(TaskRefDocsEntity::getTaskId, taskId);
        this.remove(queryWrapper);
    }
}
