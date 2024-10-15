package com.erp.server.mrp.calculation.service;

import java.util.List;

public interface ShopInfoService {
    /**
     * 根据平台查询店铺
     */
    List<String> getShopInfoByPlatform(String platform);

}
