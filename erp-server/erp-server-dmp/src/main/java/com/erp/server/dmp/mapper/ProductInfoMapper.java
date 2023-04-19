package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductInfoMapper extends BaseMapper<ProductInfoEntity> {
    /**
     * 根据主键Id查询产品表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param id
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    ProductInfoEntity getProductInfoById(@Param("id") String id);
}
