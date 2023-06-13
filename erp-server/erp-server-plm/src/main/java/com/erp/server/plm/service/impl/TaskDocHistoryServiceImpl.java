package com.erp.server.plm.service.impl;

import com.erp.model.plm.entity.TaskDocHistoryEntity;
import com.erp.server.plm.mapper.TaskDocHistoryMapper;
import com.erp.server.plm.service.TaskDocHistoryService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
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



}
