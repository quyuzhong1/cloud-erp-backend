package com.erp.server.plm.service.impl;

import com.erp.model.plm.entity.TaskConcernEntity;
import com.erp.server.plm.mapper.TaskConcernMapper;
import com.erp.server.plm.service.TaskConcernService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 任务关注的人 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-19
 */
@Service
public class TaskConcernServiceImpl extends SuperServiceImpl<TaskConcernMapper, TaskConcernEntity> implements TaskConcernService {

}
