package com.erp.server.mrp.calculation.service.impl;

import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.mrp.calculation.service.ShopInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ShopInfoServiceImpl implements ShopInfoService {

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Override
    public List<String> getShopInfoByPlatform(String platform) {
        return shopInfoFeign.listShopInfoByPlatform(platform);
    }
}
