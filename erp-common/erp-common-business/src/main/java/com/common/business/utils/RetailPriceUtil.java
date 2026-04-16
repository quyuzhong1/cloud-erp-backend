package com.common.business.utils;

import cn.hutool.core.text.CharSequenceUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标准零售价格工具类
 */
public class RetailPriceUtil {

    /**
     * 校验和获取零售价
     * @param allocationSettingStr
     * @param retailPricetotalMap
     * @param currency
     * @param childSkuIds
     * @return
     */
    public static Map<String, BigDecimal> checkAndGetRetailPrice(String allocationSettingStr,
                                                          Map<String, BigDecimal> retailPricetotalMap,
                                                          String currency,
                                                          List<String> childSkuIds
    ) {
        // 未开启配置
        if (!"retail_std".equals(allocationSettingStr)){
            return new HashMap<>();
        }
        // 订单币种零售价优先
        Map<String,BigDecimal> orderCurrencyRetailPriceTotalMap = filterCurrencyRetailPrice(childSkuIds, currency, retailPricetotalMap);
        if (orderCurrencyRetailPriceTotalMap != null) return orderCurrencyRetailPriceTotalMap;
        Map<String, BigDecimal> cnyRetailPriceTotalMap = filterCurrencyRetailPrice(childSkuIds, "CNY", retailPricetotalMap);
        if (cnyRetailPriceTotalMap != null) return cnyRetailPriceTotalMap;
        return new HashMap<>();
    }


    /**
     * 过滤相同的SKU和币种的零售价总金额
     */
    public static Map<String, BigDecimal> filterCurrencyRetailPrice(List<String> childSkuIds, String currency, Map<String, BigDecimal> retailPricetotalMap) {
        List<String> cnyCurrencySkuKeyList = childSkuIds.stream().map(e -> CharSequenceUtil.format("{}|{}", e, currency)).collect(Collectors.toList());
        if (cnyCurrencySkuKeyList.stream().allMatch(retailPricetotalMap::containsKey)) {
            if (cnyCurrencySkuKeyList.stream().allMatch(retailPricetotalMap::containsKey)) {
                // 过滤retailPricetotalMap，得到存在orderCurrencySkuKeyList的key和value的Map
                return retailPricetotalMap
                        .entrySet()
                        .stream()
                        .filter(entry -> cnyCurrencySkuKeyList.contains(entry.getKey()))
                        .collect(Collectors.toMap(e-> e.getKey().split("\\|")[0], Map.Entry::getValue, (v1, v2) -> v1));
            }
        }
        return null;
    }
}
