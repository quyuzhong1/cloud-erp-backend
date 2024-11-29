package com.erp.server.mrp.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingCalcEntity;
import com.erp.server.mrp.mapper.CfgRuleSalesDenoisingCalcMapper;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingCalcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
        return listByCfgRuleCalcIds(Collections.singletonList(id));
    }

    @Override
    public List<CfgRuleSalesDenoisingCalcEntity> listByCfgRuleCalcIds(List<String> cfgRuleCalcIds) {
        return list(Wrappers.<CfgRuleSalesDenoisingCalcEntity>lambdaQuery()
                .in(CfgRuleSalesDenoisingCalcEntity::getCfgRuleCalcId,cfgRuleCalcIds)
                .orderByAsc(CfgRuleSalesDenoisingCalcEntity::getIndex));
    }
}
