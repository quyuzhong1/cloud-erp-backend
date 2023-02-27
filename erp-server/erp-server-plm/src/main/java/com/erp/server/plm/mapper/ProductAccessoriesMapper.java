package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductAccessoriesDTO;
import com.erp.model.plm.entity.ProductAccessoriesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品包装/辅料信息(ProductAccessories)表数据库访问层
 *
 * @author yl
 * @since 2023-02-25 12:58:11
 */
@Mapper
public interface ProductAccessoriesMapper extends BaseMapper<ProductAccessoriesEntity> {


    List<ProductAccessoriesDTO> getByProductId(@Param("productId") String productId);
}

