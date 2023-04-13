package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.QcRuleEntity;
import com.erp.server.wms.mapper.QcRuleMapper;
import com.erp.server.wms.service.QcRuleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 质检规则 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
@Service
public class QcRuleServiceImpl extends SuperServiceImpl<QcRuleMapper, QcRuleEntity> implements QcRuleService {

}
