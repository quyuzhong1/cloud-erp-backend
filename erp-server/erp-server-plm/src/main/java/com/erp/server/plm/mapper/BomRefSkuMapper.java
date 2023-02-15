package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.BomSkuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * bom 与sku关系表(BomRefSku)表数据库访问层
 *
 * @author yl
 * @since 2023-01-09 11:45:27
 */
@Mapper
public interface BomRefSkuMapper extends BaseMapper<BomSkuEntity> {

    

}

