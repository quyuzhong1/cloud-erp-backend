package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * FBA货件签收信息 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
@Mapper
public interface FbaShipmentReceiveMapper extends BaseMapper<FbaShipmentReceiveEntity> {
    /**
     * 汇总签收数量
     * @param dto
     * @return
     */
    List<FirstMileDeliveryDTO.ReceiveDTO> countReceiveQtyByParams(@Param("params")FirstMileDeliveryDTO.RequestReceiveDTO dto);
}
