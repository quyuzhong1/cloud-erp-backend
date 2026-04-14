package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.QcSamplingCodeRuleEntity;
import com.erp.server.wms.mapper.QcSamplingCodeRuleMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcSamplingCodeRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * GB/T2828.1-2012 批量-样本量字码映射表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-03-19
 */
@Slf4j
@Service
public class QcSamplingCodeRuleServiceImpl extends SuperServiceImpl<QcSamplingCodeRuleMapper, QcSamplingCodeRuleEntity> implements QcSamplingCodeRuleService {
    @Resource
    private OperateLogService operateLogService;

}
