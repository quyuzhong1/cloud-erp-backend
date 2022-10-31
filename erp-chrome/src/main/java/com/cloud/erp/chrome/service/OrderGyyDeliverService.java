package com.cloud.erp.chrome.service;

import com.cloud.erp.chrome.dto.GyyShipmentsDTO;
import com.cloud.erp.chrome.entity.OrderGyyDeliverEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 管易云ERP 发货信息表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-01v
 */
public interface OrderGyyDeliverService extends IService<OrderGyyDeliverEntity> {

    //保存管易云 发货信息表
    void saveDeliverCsvByUrl(GyyShipmentsDTO dto);
}
