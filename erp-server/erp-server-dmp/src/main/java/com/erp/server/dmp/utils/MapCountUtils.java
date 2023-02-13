package com.erp.server.dmp.utils;

import cn.hutool.core.util.StrUtil;

import java.util.Map;

/**
 * Map过程处理工具
 *
 * @Author Cloud
 * @Date 2023/2/9 11:42
 **/
public class MapCountUtils {

    /**
     * 使用map处理订单中数据sku重复问题
     * @param skuCountMap
     * @param skuNo
     * @param erpOrderItemId
     * @return
     */
    public static String getErpOrderItemId(Map<String, Integer> skuCountMap, String skuNo, String erpOrderItemId) {
        if(null == skuCountMap.get(skuNo)){
            skuCountMap.put(skuNo, 1);
        }else {
            Integer count = skuCountMap.get(skuNo);
            skuCountMap.put(skuNo, count + 1);
            erpOrderItemId = StrUtil.format("{}_{}", erpOrderItemId, count);
        }
        return erpOrderItemId;
    }
}
