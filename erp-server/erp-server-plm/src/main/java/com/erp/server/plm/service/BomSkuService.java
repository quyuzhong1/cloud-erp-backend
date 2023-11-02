package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.dto.ProductBomInfoDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.BomSkuEntity;

import java.util.List;

/**
 * bom 与sku关系表(BomRefSku)表服务接口
 *
 * @author yl
 * @since 2023-01-09 11:45:27
 */
public interface BomSkuService extends IService<BomSkuEntity> {


    void saveBomSku(String bomId, List<BomSkuDTO> bomSkuList);

    List<BomSkuDTO> getByBomId(String bomId);

    void updateBomSku(String bomId, List<BomSkuDTO> bomSkuList);

    void deleteByBomId(String id);
    /**
     * @description: 根据父级skuId查询
     * @author Will
     * @date: 2023/3/7 14:54
     * @param parentSkuId
     * @return List<BomSkuEntity>
     */
    List<BomSkuEntity> getByParentSkuId(String parentSkuId);

    /**
     * @description: 根据父级skuNo查询
     * @author Will
     * @date: 2023/8/17 14:50
     * @param parentSkuNos
     * @return List<BomSkuEntity>
     */
    List<BomSkuEntity> listByParentSkuNos(List<String> parentSkuNos);
    /**
     * @description: 查询所有状态子集SKU
     * @author Will
     * @date: 2023/5/17 9:40
     * @param parentSkuIds
     * @return List<BomChildrenSkuDTO>
     */
    List<BomChildrenSkuDTO> listAllBomChildBySkuIds(List<String> parentSkuIds);
    /**
     * @description: 查询子集SKU
     * @author Will
     * @date: 2023/5/17 9:40
     * @param parentSkuIds
     * @return List<BomChildrenSkuDTO>
     */
    List<BomChildrenSkuDTO> listBomChildBySkuIds(List<String> parentSkuIds);

    /**
     * 查询子集SKU
     * @Author Luo_WG
     * @Date 2023/9/14 12:09
     * @param parentSkuNos
     * @return java.util.List<com.erp.model.plm.dto.BomChildrenSkuDTO>
     **/
    List<BomChildrenSkuDTO> listBomChildBySkuNos(List<String> parentSkuNos);

    /**
     * @description: 根据父级skuIds查询BOM
     * @author Will
     * @date: 2023/5/31 11:00
     * @param parentSkuIds
     * @return List<BomInfoEntity>
     */
    List<BomInfoEntity> listBomByParentSkuIds(List<String> parentSkuIds);

    /**
     * @description: 根据父级skuNos查询已归档BOM
     * @author zhangchunlin
     * @date: 2023/6/30 11:00
     * @param parentSkuNos
     * @return List<BomInfoEntity>
     */
    List<BomInfoEntity> listBomByParentSkuNos(List<String> parentSkuNos);

    /**
     * @description: 根据父级skuNos查询未作废BOM
     * @author Will
     * @date: 2023/8/30 11:00
     * @param parentSkuNos
     * @return List<BomInfoEntity>
     */
    List<BomInfoEntity> listAllBomByParentSkuNos(List<String> parentSkuNos);

    /**
     * 根据sku查询bom
     * @Author Luo_WG
     * @Date 2023/9/4 19:38
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.dto.BomChildrenSkuDTO>
     **/
    List<BomChildrenSkuDTO> listBomBySkuIds(List<String> skuIds);


    /**
     * 根据bom id 获取到 bom到sku
     * @author yl
     * @date 2023-10-11 19:23
     * @param bomId
     * @return java.util.List<com.erp.model.plm.entity.BomSkuEntity>
     */
    List<BomSkuEntity> listBomSkuByBomId(String bomId);

    /**
     * 查询sku版本信息
     * @Author Luo_WG
     * @Date 2023/11/2 8:57
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.ProductBomInfoDTO.skuBomVersion>
     **/
    List<ProductBomInfoDTO.skuBomVersion> listBomVersionBySkuNos(ProductBomInfoDTO.skuBomVersionParams dto);
}
