package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.ProductPropertiesEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * sku与配置字段关系表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-08
 */
@Mapper
public interface ProductPropertiesMapper extends BaseMapper<ProductPropertiesEntity> {

}
