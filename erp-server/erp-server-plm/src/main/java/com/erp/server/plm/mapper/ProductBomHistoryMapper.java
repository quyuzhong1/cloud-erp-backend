package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * bom 历史表(ProductBomHistory)表数据库访问层
 *
 * @author yl
 * @since 2023-01-11 14:04:50
 */
@Mapper
public interface ProductBomHistoryMapper extends BaseMapper<ProductBomHistoryEntity> {

    

}

