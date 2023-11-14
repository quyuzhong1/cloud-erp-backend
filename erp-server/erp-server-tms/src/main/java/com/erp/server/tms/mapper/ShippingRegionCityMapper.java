package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.entity.ShippingRegionCityEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 运费规则分区城市表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-11-07
 */
@Mapper
public interface ShippingRegionCityMapper extends BaseMapper<ShippingRegionCityEntity> {

    /**
     * @description: 查询分区城市
     * @author Will
     * @date: 2023/11/14 17:26
     * @param params
     * @return List<String>
     */
    List<String> listRegionCity(@Param("params") ShippingCalculationDTO.ListRegionCityParamDTO params);
}
