package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingCalcEntity;

import java.util.List;

/**
 * <p>
 * 试算销量去噪信息 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CfgRuleSalesDenoisingCalcService extends SuperService<CfgRuleSalesDenoisingCalcEntity> {


    List<CfgRuleSalesDenoisingCalcEntity> listByCfgRuleCalcId(String id);
}
