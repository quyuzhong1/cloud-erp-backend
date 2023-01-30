package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.vo.BomVersionVO;

import java.util.List;

/**
 * bom 历史表(ProductBomHistory)表服务接口
 *
 * @author yl
 * @since 2023-01-11 14:04:50
 */
public interface ProductBomHistoryService  extends IService<ProductBomHistoryEntity> {


    void insert(BomInfoEntity bom, List<BomSkuDTO> bomSkuList);

    void deleteByBomId(String id);

    List<BomVersionVO> getVersionList(String id);
}
