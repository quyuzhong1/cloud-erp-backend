package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Entity com.erp.model.plm.entity.DmpOrderItem
 */
@Mapper
public interface DmpOrderItemMapper extends BaseMapper<DmpOrderItemEntity> {
    /**
     * 根据sku以及年份获取订单明细表Id
     * @Author Luo_WG
     * @Date 2023/4/19 18:30
     **/
    List<String> getItemIdBySkuAndYear(@Param("year") String year, @Param("skuNo") String skuNo);


    List<Map<String,String>> getOrderListingTime(@Param("skuNoList") List<String> skuNoList);
}




