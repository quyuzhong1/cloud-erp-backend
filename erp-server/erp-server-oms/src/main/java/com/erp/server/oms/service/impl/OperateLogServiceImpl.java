package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.OperateLogEntity;
import com.erp.server.oms.mapper.OperateLogMapper;
import com.erp.server.oms.service.OperateLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Service
public class OperateLogServiceImpl extends SuperServiceImpl<OperateLogMapper, OperateLogEntity> implements OperateLogService {

}
