package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.server.dmp.mapper.AmzReportTaskMapper;
import com.erp.server.dmp.service.AmzReportTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 亚马逊报告请求记录 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@Service
public class AmzReportTaskServiceImpl extends SuperServiceImpl<AmzReportTaskMapper, AmzReportTaskEntity> implements AmzReportTaskService {

}
