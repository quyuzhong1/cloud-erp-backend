package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Date;

@Mapper
public interface DmpShopChangeLogMapper extends BaseMapper<DmpShopChangeLogEntity> {

    /**
     * 查询店铺负责人
     * @Author Luo_WG
     * @Date 2022/12/29 16:21
     * @param shopId shopId
     * @param date date
     * @return com.erp.model.dmp.entity.DmpShopChangeLogEntity
     **/
    DmpShopChangeLogEntity getShopChargeName(@Param("shopId") String shopId, @Param("date") LocalDateTime date);
}
