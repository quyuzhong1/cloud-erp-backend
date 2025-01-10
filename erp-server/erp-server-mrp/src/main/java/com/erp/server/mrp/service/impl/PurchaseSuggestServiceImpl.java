package com.erp.server.mrp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.erp.server.mrp.mapper.PurchaseSuggestMapper;
import com.erp.server.mrp.service.PurchaseSuggestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 建议采购 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@Service
public class PurchaseSuggestServiceImpl extends SuperServiceImpl<PurchaseSuggestMapper, PurchaseSuggestEntity> implements PurchaseSuggestService {

    @Override
    public List<PurchaseSuggestEntity> listGeneratePurchaseSuggestMerge(String platform,List<String> skuIdList,List<LocalDate> suggestPurchaseDateList) {
        return baseMapper.listGeneratePurchaseSuggestMerge(platform,skuIdList,suggestPurchaseDateList);
    }

}
