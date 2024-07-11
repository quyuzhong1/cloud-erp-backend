package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.BiShopChangeLogEntity;

import java.time.LocalDateTime;

public interface BiShopChangeLogService extends IService<BiShopChangeLogEntity> {
    /**
     * 查询店铺负责人
     * @Author Luo_WG
     * @Date 2022/12/29 16:21
     * @param shopId shopId
     * @param date date
     * @return com.erp.model.dmp.entity.DmpShopChangeLogEntity
     **/
    BiShopChangeLogEntity getShopChargeName(String shopId, LocalDateTime date);
}
