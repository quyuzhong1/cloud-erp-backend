package com.erp.server.oms.kingdee;

import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;

import java.util.List;
import java.util.Map;

public interface SyncSoB2cService {

    /**
     * 同步订单到数帝云的字段映射处理
     * @param soB2cEntity
     * @param soB2cDetailEntity
     * @param operate
     * @return
     */
    Map<String, Object> syncDataToSdyFieldHandler(SoB2cEntity soB2cEntity,
                                                  List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                  SoB2cDetailEntity soB2cDetailEntity,
                                                  String operate,
                                                  List<SkuVO> skuVOList,
                                                  List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                  List<ProductDetailEntity> parentSkuList,
                                                  List<ListingInfoEntity> listingInfoEntities,
                                                  List<CurrencyDTO.ViewDTO> currencyList,
                                                  List<DictCurrencyEntity> dictCurrencyEntities,
                                                  List<ShopInfoEntity> shopInfoList,
                                                  List<CustomerInfoEntity> customerInfoList,
                                                  List<BaseIdDTO.CodeDTO> companyEntities,
                                                  List<DictBasicEntity> dictBasicEntityList,
                                                  List<DictBasicEntity> dictList);

    void syncDataToSdy(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> soB2cDetailEntityList, String operate);

    void syncDataToSdy(SoB2cEntity soB2cEntity,
                       List<SoB2cDetailEntity> detailEntityList,
                       String operate,
                       List<SkuVO> skuVOList,
                       List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                       List<ProductDetailEntity> parentSkuList,
                       List<ListingInfoEntity> listingInfoEntities,
                       List<CurrencyDTO.ViewDTO> currencyList,
                       List<DictCurrencyEntity> dictCurrencyEntities,
                       List<ShopInfoEntity> shopInfoList,
                       List<CustomerInfoEntity> customerInfoList,
                       List<BaseIdDTO.CodeDTO> companyEntities,
                       List<DictBasicEntity> dictBasicEntityList,
                       List<DictBasicEntity> dictList);
}
