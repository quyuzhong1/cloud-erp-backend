package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.tms.dto.LogisticsBillDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    /**
     * 根据物流单号或者运单号查询
     * @author will
     * @date 2024/7/5 10:29
     * @param logisticsCode
     * @return SoB2cLogisticsEntity
     */
    SoB2cLogisticsEntity getByTrackNoOrTransportNo(@Param("logisticsCode") String logisticsCode);

    /**
     * 根据运单号进行跟踪号更新
     * @param trackDTOS
     */
    void updateTrackNoByTransportNo(@Param("trackDTOS") List<LogisticsBillDTO.TrackDTO> trackDTOS);
}
