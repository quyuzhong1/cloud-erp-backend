package com.erp.server.wms.mapper;

import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * B2B三方发货单明细 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
@Mapper
public interface B2bThirdDeliveryDetailMapper extends BaseMapper<B2bThirdDeliveryDetailEntity> {

    List<B2bThirdDeliveryDetailEntity> listBySoDetailIds(@Param("soDetailIds") List<String> soDetailIds);
}
