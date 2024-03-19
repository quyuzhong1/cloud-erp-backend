package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.dto.ProductLogisticsShowDTO;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.ProductLogistics
 */
@Mapper
public interface ProductLogisticsMapper extends BaseMapper<ProductLogisticsEntity> {
    /**
     * @Description 产品物流信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 15:43
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    List<ProductLogisticsShowDTO> list(@Param("productId") String productId);

    /**
     * @Description 产品物流信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 15:43
     * @param skuId
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    List<ProductLogisticsShowDTO> listBySkuId(@Param("skuId") String skuId);
    /**
     * @description: 已审核备案SKU下拉
     * @author Will
     * @date: 2024/3/19 15:11
     * @return List<SelectDTO>
     */
    List<LogisticsProductDTO.SelectDTO> selectSku();
}




