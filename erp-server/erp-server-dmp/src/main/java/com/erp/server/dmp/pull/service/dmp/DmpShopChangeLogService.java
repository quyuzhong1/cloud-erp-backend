package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Date;

public interface DmpShopChangeLogService extends IService<DmpShopChangeLogEntity> {
    /**
     * 查询店铺负责人
     * @Author Luo_WG
     * @Date 2022/12/29 16:21
     * @param shopId shopId
     * @param date date
     * @return com.erp.model.dmp.entity.DmpShopChangeLogEntity
     **/
    DmpShopChangeLogEntity getShopChargeName(String shopId, LocalDateTime date);
}
