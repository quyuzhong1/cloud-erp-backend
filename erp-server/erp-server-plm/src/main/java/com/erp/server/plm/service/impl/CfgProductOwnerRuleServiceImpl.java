package com.erp.server.plm.service.impl;

import com.erp.model.plm.entity.CfgProductOwnerRuleEntity;
import com.erp.server.plm.mapper.CfgProductOwnerRuleMapper;
import com.erp.server.plm.service.CfgProductOwnerRuleService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 产品归属规则配置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@Slf4j
@Service
public class CfgProductOwnerRuleServiceImpl extends SuperServiceImpl<CfgProductOwnerRuleMapper, CfgProductOwnerRuleEntity> implements CfgProductOwnerRuleService {



}
