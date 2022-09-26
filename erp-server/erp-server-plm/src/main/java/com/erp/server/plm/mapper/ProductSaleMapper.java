package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.dto.ProductSaleShowDTO;
import com.erp.model.plm.entity.ProductSaleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.ProductSale
 */
@Mapper
public interface ProductSaleMapper extends BaseMapper<ProductSaleEntity> {
    /**
     * @Description 产品销售信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    List<ProductSaleShowDTO> list(@Param("productId") String productId);
}




