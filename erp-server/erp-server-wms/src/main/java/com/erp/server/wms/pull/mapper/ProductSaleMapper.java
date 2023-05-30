package com.erp.server.wms.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductSaleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Entity com.erp.model.plm.entity.ProductSale
 */
@Mapper
public interface ProductSaleMapper extends BaseMapper<ProductSaleEntity> {


    /**
     * 更新所有字段包括逻辑删除字段
     * @param productSaleEntity
     * @return
     */
    int updateAllById(ProductSaleEntity productSaleEntity);

    /**
     * 根据id获取
     * @param id
     * @return
     */
    ProductSaleEntity selectById(String id);


}




