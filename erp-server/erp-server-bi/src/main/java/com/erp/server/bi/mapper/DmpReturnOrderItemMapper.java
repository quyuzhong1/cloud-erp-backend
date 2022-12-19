package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpReturnOrderItem
 */
@Mapper
public interface DmpReturnOrderItemMapper extends BaseMapper<DmpReturnOrderItemEntity> {

    /**
     * 统计退货金额
     * @param settleMethod
     * @param sku
     * @return
     */

    BigDecimal sumReturnAmountBySku(@Param("sku") List<String> sku,@Param("settleMethod") Integer settleMethod);
}




