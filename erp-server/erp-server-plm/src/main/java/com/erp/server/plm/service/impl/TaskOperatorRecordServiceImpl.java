package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.TaskOperatorRecordEntity;
import com.erp.server.plm.mapper.TaskOperatorRecordMapper;
import com.erp.server.plm.service.TaskOperatorRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname TaskOperatorRecordServiceImpl
 * @Description TODO
 * @Date 2022-10-20 15:43
 * @Created by yl
 */
@Service
public class TaskOperatorRecordServiceImpl extends ServiceImpl<TaskOperatorRecordMapper, TaskOperatorRecordEntity> implements TaskOperatorRecordService {

    /**
     * 批量保存记录
     *
     * @param taskIds
     * @param beforeState
     * @param afterState
     * @param uid
     * @param userName
     * @param comment
     * @return void
     * @author yl
     * @date 2022-10-20 16:14
     */

    @Override
    public void batchSaveRecord(List<String> taskIds, Integer beforeState, Integer afterState, String uid, String userName, String comment) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            List<TaskOperatorRecordEntity> addList = new ArrayList<>();
            for (String taskId : taskIds) {
                TaskOperatorRecordEntity record = new TaskOperatorRecordEntity();
                record.setOperatorId(uid);
                record.setOperatorName(userName);
                record.setBeforeState(beforeState);
                record.setAfterState(afterState);
                record.setTaskId(taskId);
                addList.add(record);
            }
            this.saveBatch(addList);
        }


    }
}
