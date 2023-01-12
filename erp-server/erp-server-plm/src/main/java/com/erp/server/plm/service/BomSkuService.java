package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BomSkuDTO;
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

    void updateBomSku(String id, List<BomSkuDTO> bomSkuList);
}
