package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductDetailMapper extends BaseMapper<ProductDetailEntity> {
    /**
     * 根据主键Id查询产品Sku表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:29
     * @param id
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    ProductDetailEntity getProductDetailById(@Param("id") String id);
}
