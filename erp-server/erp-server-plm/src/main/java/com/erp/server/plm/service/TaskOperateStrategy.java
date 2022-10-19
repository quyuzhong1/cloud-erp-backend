package com.erp.server.plm.service;

import java.util.List;

/**
 * 任务操作策略
 *
 * @Classname TaskOperateStrategy
 * @Description TODO
 * @Date 2022-10-18 15:14
 * @Created by yl
 */
public interface TaskOperateStrategy {

     //实现 更改任务状态
    Boolean updateTaskState(List<String> taskIds, Integer state, String userId);
}
