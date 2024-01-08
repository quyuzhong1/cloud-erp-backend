package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * B2C销售订单物流信息表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Mapper
public interface SoB2cLogisticsMapper extends BaseMapper<SoB2cLogisticsEntity> {

    /**
     * 获取到跟踪单号为空的
     * @description
     * @author Lambda
     * @return 
     * @create 2024-01-05 9:31
     */
    List<SoB2cLogisticsDTO.TrackNoDTO> listTrackNoEmptyList();
}
