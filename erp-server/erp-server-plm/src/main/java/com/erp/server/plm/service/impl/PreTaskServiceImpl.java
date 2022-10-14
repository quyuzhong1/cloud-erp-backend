package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.SetPreTaskDTO;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.server.plm.mapper.PreTaskMapper;
import com.erp.server.plm.service.PreTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 *
 */
@Service
public class PreTaskServiceImpl extends ServiceImpl<PreTaskMapper, PreTaskEntity>
        implements PreTaskService {


    /**
     * 保存前置任务
     *
     * @param taskId
     * @param preTaskIdList
     * @return void
     * @author yl
     * @date 2022-10-12 14:28
     */

    @Override
    public void savePreTask(String taskId, List<String> preTaskIdList) {
        if(CollectionUtils.isNotEmpty(preTaskIdList)){
            //先删除前置任务
            removePreTaskByTaskId(taskId, preTaskIdList);
            if (CollectionUtils.isNotEmpty(preTaskIdList)) {
                List<PreTaskEntity> addList = new ArrayList<>();
                for (String preTaskId : preTaskIdList) {
                    PreTaskEntity entity = new PreTaskEntity();
                    entity.setPreTaskId(preTaskId);
                    entity.setTaskId(taskId);
                    addList.add(entity);
                }
                this.saveBatch(addList);
            }
        }

    }


    /**
     * 删除前置任务
     *
     * @param taskId
     * @param preTaskIdList
     * @return void
     * @author yl
     * @date 2022-10-13 9:39
     */
    private void removePreTaskByTaskId(String taskId, List<String> preTaskIdList) {
        LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreTaskEntity::getTaskId, taskId);
        queryWrapper.in(PreTaskEntity::getPreTaskId, preTaskIdList);
        this.remove(queryWrapper);
    }

    /**
     * 添加前置任务
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-12 14:32
     */

    @Override
    public Boolean addPreTask(SetPreTaskDTO dto) {
        PreTaskEntity entity = new PreTaskEntity();
        entity.setTaskId(dto.getTaskId());
        entity.setPreTaskId(dto.getPreTaskId());
        return this.save(entity);
    }

    /**
     * 移除前置任务
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean removePreTask(SetPreTaskDTO dto) {
        LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreTaskEntity::getTaskId, dto.getTaskId());
        queryWrapper.eq(PreTaskEntity::getPreTaskId, dto.getPreTaskId());
        return remove(queryWrapper);
    }

    /**
     * 根据任务id 获取任务的前置任务id
     *
     * @param taskId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-12 14:45
     */
    @Override
    public List<String> getPreTaskIdList(String taskId) {
        LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreTaskEntity::getTaskId, taskId);
        queryWrapper.select(PreTaskEntity::getPreTaskId);
        return this.listObjs(queryWrapper, Object::toString);
    }
}




