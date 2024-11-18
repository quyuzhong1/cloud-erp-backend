package com.erp.server.mrp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingCalcEntity;
import com.erp.server.mrp.mapper.CfgRuleSalesDenoisingCalcMapper;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingCalcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 试算销量去噪信息 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CfgRuleSalesDenoisingCalcServiceImpl extends SuperServiceImpl<CfgRuleSalesDenoisingCalcMapper, CfgRuleSalesDenoisingCalcEntity> implements CfgRuleSalesDenoisingCalcService {

    @Override
    public List<CfgRuleSalesDenoisingCalcEntity> listByCfgRuleCalcId(String id) {

        return lambdaQuery().eq(CfgRuleSalesDenoisingCalcEntity::getCfgRuleCalcId, id).orderByAsc(CfgRuleSalesDenoisingCalcEntity::getIndex).list();
    }
}
