package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ProductDetailMapper extends BaseMapper<ProductDetailEntity> {
    /**
     * 根据主键Id查询产品Sku表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:29
     * @param ids
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    List<ProductDetailEntity> ListProductDetailByIds(@Param("ids") List<String> ids);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/6/13 11:03
     * @param record record
     * @return int
     **/
    int updateByPrimaryKeySelective(ProductDetailEntity record);

    /**
     * 批量修改
     * @Author Luo_WG
     * @Date 2023/6/13 11:03
     * @param list
     * @return int
     **/
    int updateBatchSelective(List<ProductDetailEntity> list);
}
