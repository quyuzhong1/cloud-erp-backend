package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductChangeDetailsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 变更管理变更实体的信息表(ProductChangeDetails)表数据库访问层
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Mapper
public interface ProductChangeDetailsMapper  extends BaseMapper<ProductChangeDetailsEntity> {

    

}

