package com.cloud.erp.chrome.service;

import com.cloud.erp.chrome.dto.GyyShipmentsDTO;
import com.cloud.erp.chrome.entity.GyuShipmentsInfoEntity;
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
