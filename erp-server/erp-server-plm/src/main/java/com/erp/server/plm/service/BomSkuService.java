package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuDTO;
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
     * @description: 查询子集SKU
     * @author Will
     * @date: 2023/5/17 9:40
     * @param parentSkuIds
     * @return List<BomChildrenSkuDTO>
     */
    List<BomChildrenSkuDTO> listBomChildBySkuIds(List<String> parentSkuIds);
    /**
     * @description: 根据父级skuIds查询BOM
     * @author Will
     * @date: 2023/5/31 11:00
     * @param parentSkuIds
     * @return List<BomInfoEntity>
     */
    List<BomInfoEntity> listBomByParentSkuIds(List<String> parentSkuIds);
}
