package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;

import java.util.List;

/**
 * @author Lambda
 * @Classname DictCurrencyService
 * @Description TODO
 * @Date 2023-03-21 17:21
 * @Created by yl
 */
public interface DictCurrencyService extends SuperService<DictCurrencyEntity> {
    
    /**
     * 获取货币列表
     * @author yl
     * @date 2023-03-21 17:27
     * @param
     * @return java.util.List<com.erp.model.sys.dto.CurrencyDTO.ViewDTO>
     */
    List<CurrencyDTO.ViewDTO> getList();
    /**
     * @description: 根据币种查询
     * @author Will
     * @date: 2023/3/27 18:03
     * @param currencyList
     * @return List<CurrencyDTO.ViewDTO>
     */
    List<CurrencyDTO.ViewDTO> listByCurrency(List<String> currencyList);
}
