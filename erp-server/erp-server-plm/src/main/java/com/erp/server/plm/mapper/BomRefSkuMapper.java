package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.BomSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * bom 与sku关系表(BomRefSku)表数据库访问层
 *
 * @author yl
 * @since 2023-01-09 11:45:27
 */
@Mapper
public interface BomRefSkuMapper extends BaseMapper<BomSkuEntity> {

    /**
     * @description: 根据父SKU查询子集SKU
     * @author Will
     * @date: 2023/5/17 9:43
     * @param parentSkuId
     * @return List<BomChildrenSkuDTO>
     */
    List<BomChildrenSkuDTO> listBomChildBySkuId(@Param("parentSkuId") String parentSkuId);
}

