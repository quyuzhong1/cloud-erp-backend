package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomDTO;
import com.erp.model.plm.entity.BomInfoEntity;
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
     * @description: 根据父SKU查询所有子集SKU
     * @author Will
     * @date: 2023/5/17 9:43
     * @param parentSkuIds
     * @return List<BomChildrenSkuDTO>
     */
    List<BomChildrenSkuDTO> listAllBomChildBySkuIds(@Param("parentSkuIds") List<String> parentSkuIds);
    /**
     * @description: 根据父SKU查询子集SKU
     * @author Will
     * @date: 2023/5/17 9:43
     * @param parentSkuIds
     * @return List<BomChildrenSkuDTO>
     */
    List<BomChildrenSkuDTO> listBomChildBySkuIds(@Param("parentSkuIds") List<String> parentSkuIds);

    /**
     * @description: 根据父SKU查询子集SKU
     * @Author Luo_WG
     * @Date 2023/9/14 12:03
     * @param parentSkuNos
     * @return java.util.List<com.erp.model.plm.dto.BomChildrenSkuDTO>
     **/
    List<BomChildrenSkuDTO> listBomChildBySkuNos(@Param("parentSkuNos") List<String> parentSkuNos);

    /**
     * @description: 根据父级SKU查询BOM
     * @author Will
     * @date: 2023/5/31 11:05
     * @param parentSkuIds
     * @return List<BomInfoEntity>
     */
    List<BomInfoEntity> listBomByParentSkuIds(@Param("parentSkuIds") List<String> parentSkuIds);

    /**
     * @description: 根据父级SKU NO查询BOM
     * @author zhangchunlin
     * @date: 2023/6/30 11:05
     * @param parentSkuNos
     * @return List<BomInfoEntity>
     */
    List<BomInfoEntity> listBomByParentSkuNos(@Param("parentSkuNos") List<String> parentSkuNos);

    /**
     * @description: 根据父级skuNos查询未作废BOM
     * @author Will
     * @date: 2023/8/30 11:05
     * @param parentSkuNos
     * @return List<BomInfoEntity>
     */
    List<BomInfoEntity> listAllBomByParentSkuNos(@Param("parentSkuNos") List<String> parentSkuNos);


    /**
     * 根据KU查询Bom
     * @Author Luo_WG
     * @Date 2023/9/4 19:36
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.dto.BomChildrenSkuDTO>
     **/
    List<BomChildrenSkuDTO> listBomBySkuIds(@Param("skuIds") List<String> skuIds);

    List<BomDTO.BomSku> listBySkuIds(@Param("skuIdList")List<String> skuIdList);
}

