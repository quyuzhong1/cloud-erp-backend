package com.erp.server.wms.kingdee;

import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.entity.*;

import java.util.List;
import java.util.Map;

public interface SyncSoReturnInstockService {
    /**
     * 同步数帝云字段映射处理
     */
    Map<String, Object> syncDataToSdyFieldHandler(SoReturnInstockEntity entity,
                                                  SoReturnInstockDetailEntity detailEntity,
                                                  String operate,
                                                  List<SkuVO> skuVOList,
                                                  List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                  List<CurrencyDTO.ViewDTO> currencyList,
                                                  List<ProductDetailEntity> parentSkuList,
                                                  List<CustomerInfoEntity> customerInfoList,
                                                  List<BaseIdDTO.CodeDTO> companyEntities,
                                                  List<DictBasicEntity> dictBasicEntityList,
                                                  List<SoReturnEntity> soReturnEntityList,
                                                  List<SoReturnReceiveEntity> soReturnReceiveEntityList,
                                                  List<SoReturnEntity> receiveReturnList);

    /**
     * 同步数帝云
     */
    void syncDataToSdy(SoReturnInstockEntity entity,
                       List<SoReturnInstockDetailEntity> detailEntities,
                       String operate,
                       List<SkuVO> skuVOList,
                       List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                       List<CurrencyDTO.ViewDTO> currencyList,
                       List<ProductDetailEntity> parentSkuList,
                       List<CustomerInfoEntity> customerInfoList,
                       List<BaseIdDTO.CodeDTO> companyEntities,
                       List<DictBasicEntity> dictBasicEntityList,
                       List<SoReturnEntity> soReturnEntityList,
                       List<SoReturnReceiveEntity> soReturnReceiveEntityList,
                       List<SoReturnEntity> receiveReturnList);
}
