package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;

import java.util.List;

/**
 * bom历史表与sku关系表(ProductBomSkuHistory)表服务接口
 *
 * @author yl
 * @since 2023-01-11 14:04:52
 */
public interface ProductBomSkuHistoryService  extends IService<ProductBomSkuHistoryEntity> {


    void saveBomSku(String id, List<BomSkuDTO> bomSkuList);

    List<ProductBomSkuHistoryEntity> getSkuByHistoryIds(List<String> bomHistoryIds);
}
