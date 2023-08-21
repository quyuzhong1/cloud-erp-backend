package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname ProductBomSkuHistoryMapper

 * @Date 2023-01-11 14:12
 * @Created by yl
 */
@Mapper
public interface ProductBomSkuHistoryMapper  extends BaseMapper<ProductBomSkuHistoryEntity> {

    List<BomChildrenSkuDTO> listHistoryBomChildBySkuIds(@Param("parentSkuIds") List<String> skuIds);
}
