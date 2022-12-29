package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface DmpShopChangeLogMapper extends BaseMapper<DmpShopChangeLogEntity> {

    DmpShopChangeLogEntity getShopChargeName(@Param("shopId") String shopId, @Param("date") Date date);
}
