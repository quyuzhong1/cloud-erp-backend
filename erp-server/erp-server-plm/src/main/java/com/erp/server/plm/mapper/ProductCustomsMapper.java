package com.erp.server.plm.mapper;
import com.erp.model.plm.dto.ProductCustomsSkuDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
 */
@Mapper
public interface ProductCustomsMapper extends BaseMapper<ProductCustomsEntity> {


    /**
     * 根据产品id查询目的国海关编码
     * @Author Luo_WG
     * @Date 2023/6/15 16:52
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.entity.ProductCustomsEntity>
     **/
    List<ProductCustomsEntity> listBySkuIds(List<String> skuIds);

    /**
     * 根据产品id查询目的国海关编码
     * @Author Luo_WG
     * @Date 2023/6/15 16:52
     * @param skuId
     * @return java.util.List<com.erp.model.plm.entity.ProductCustomsEntity>
     **/
    List<ProductCustomsEntity> listBySkuId(String skuId);

    List<ProductCustomsEntity> listProductCustomsBySkuIds(@Param("dto") ProductCustomsSkuDTO dto);
}
