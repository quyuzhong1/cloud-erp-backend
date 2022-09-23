package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductManyDetailDTO;
import com.erp.model.plm.dto.ProductNoDetailDTO;
import com.erp.model.plm.dto.ProductDetailShowDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductDetailMapper extends BaseMapper<ProductDetailEntity> {
    /**
    * @Description
    * @Author Luo_WG
    * @Date 2022/9/22 10:47
    * @param sku:此处可能是spu，需求界面只有一个输入框可输入spu或者sku查询
    * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
    **/
    List<ProductDetailShowDTO> list(@Param("sku") String sku);

    /**
    * @Description 无规格产品信息明细
    * @Author Luo_WG
    * @Date 2022/9/22 14:09
    * @param productId:产品信息表id
    * @return java.util.List<com.erp.model.plm.dto.ProductNoDetailDTO>
    **/
    ProductNoDetailDTO getNoSpecDetailById(@Param("productId") String productId);

    /**
    * @Description 多规格产品信息明细
    * @Author Luo_WG
    * @Date 2022/9/22 15:11
    * @param productId:产品信息表id
    * @return java.util.List<com.erp.model.plm.dto.ProductManyDetailDTO>
    **/
    ProductManyDetailDTO getManySpecDetailById(@Param("productId") String productId);
}


