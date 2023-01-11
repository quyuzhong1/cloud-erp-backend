package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductChangeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 变更信息表(ProductChange)表数据库访问层
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Mapper
public interface ProductChangeMapper extends BaseMapper<ProductChangeEntity> {

    

}

