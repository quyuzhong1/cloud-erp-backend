package com.erp.server.wms.mapper;

import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * FBI拣货明细表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Mapper
public interface FbaShipmentDetailMapper extends BaseMapper<FbaShipmentDetailEntity> {
    /**
     * 根据唯一值获取明细记录
     * @param shipmentCode
     * @param asin
     * @param msku
     * @return
     */
    FbaShipmentDetailEntity getDetail(@Param("shipmentCode") String shipmentCode, @Param("asin") String asin, @Param("msku") String msku);
}
