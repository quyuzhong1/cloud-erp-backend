package com.sdk.oms.shopify.chrome.service;

import com.sdk.oms.shopify.chrome.dto.GyyShipmentsDTO;
import com.sdk.oms.shopify.chrome.entity.GyuShipmentsInfoEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 管易云 erp 发货信息表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-08-26
 */
public interface GyyShipmentsInfoService extends IService<GyuShipmentsInfoEntity> {
    void saveShipmentsCsvByUrl(GyyShipmentsDTO dto);
}
