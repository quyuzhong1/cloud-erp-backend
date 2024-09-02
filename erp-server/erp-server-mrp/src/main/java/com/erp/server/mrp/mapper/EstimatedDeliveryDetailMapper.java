package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.EstimatedDeliveryDTO;
import com.erp.model.mrp.entity.EstimatedDeliveryDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.vo.EstimatedDeliveryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 预计发货明细 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Mapper
public interface EstimatedDeliveryDetailMapper extends BaseMapper<EstimatedDeliveryDetailEntity> {

    Page<EstimatedDeliveryVO> estimatedDelivery(@Param("page") Page<EstimatedDeliveryVO> page, @Param("params") EstimatedDeliveryDTO params);
}
