package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 10:16
 */
public interface BiSettlementExchangeRateService  extends IService<BiSettlementExchangeRateEntity> {
    /**
     * @description: 批量新增结算汇率
     * @author Will
     * @date: 2022/12/19 10:23
     * @param list
     * @return Boolean
     */
    Boolean batchAddSettlementExchangeRate(List<Map<String, Object>> list);
    /**
     * @description: 界面回显
     * @author Will
     * @date: 2022/12/20 18:49
     * @return List<Map<String>>
     */
    List<Map<String, Object>> listSettlementExchangeRate();
    /**
     * @description: 编辑
     * @author Will
     * @date: 2022/12/20 22:42
     * @param list
     * @return Boolean
     */
    Boolean batchUpdateSettlementExchangeRate(List<Map<String, Object>> list);
}
