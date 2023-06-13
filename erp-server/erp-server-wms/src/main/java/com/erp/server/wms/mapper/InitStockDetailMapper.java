package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.InitStockDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname: InitStockDetailMapper
 * @Description: TODO
 * @CreateTime: 2023-05-11  10:29
 * @Author: zhangchunlin
 */
@Mapper
public interface InitStockDetailMapper extends BaseMapper<InitStockDetailEntity> {

    /**
     * 根据仓库和状态集合判断是否存在sku（不能为作废状态）
     * @param warehouseId
     * @param warehouseLocation
     * @param skuId
     * @return
     */
    Integer countCondition(@Param(value = "warehouseId") String warehouseId,@Param(value = "warehouseLocation") String warehouseLocation, @Param(value = "skuId")  String skuId, @Param(value = "id")  String id);

}
