package com.erp.server.oms.kingdee;

import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;

import com.erp.model.sys.entity.*;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import org.apache.commons.math3.util.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SyncSoB2cService {

    /**
     * 同步订单到数帝云的字段映射处理
     *
     * @param soB2cEntity
     * @param soB2cDetailEntity
     * @param operate
     * @param deptRelationList
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
                                                  SoB2cReceiverEntity receiverEntity,
                                                  List<DictBasicEntity> omsAllDictList,
                                                  List<DictPartitionEntity> partitionEntityList,
                                                  List<DictCountryEntity> countryEntityList,
                                                  List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                  List<CfgDeptRelationEntity> deptRelationList,
                                                  List<SysDepartmentEntity> deptList);

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
                       SoB2cReceiverEntity receiverEntity,
                       List<DictBasicEntity> omsAllDictList,
                       List<DictPartitionEntity> partitionEntityList,
                       List<DictCountryEntity> countryEntityList,
                       List<DictGlobalAreaEntity> dictGlobalEntityList,
                       List<CfgDeptRelationEntity> deptRelationList,
                       List<SysDepartmentEntity> deptList);

    void syncSelfAddDataToSdy(SoB2cEntity soB2cEntity, String operateEnum);


    void syncAliExpressDataToSdy(SoB2cEntity soB2cEntity, String operateEnum);


    Map<String, Object> syncSelfAddDataToSdyFieldHandler(SoB2cEntity soB2cEntity,
                                                         List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                         SoB2cDeliveryEntity soB2cDeliveryEntity,
                                                         List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntityList,
                                                         SoB2cDeliveryDetailEntity soB2cDeliveryDetailEntity,
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
                                                         Map<String, Pair<BigDecimal, BigDecimal>> deliveryDetailPriceMap,
                                                         SoB2cReceiverEntity receiverEntity,
                                                         List<DictBasicEntity> omsAllDictList,
                                                         List<DictPartitionEntity> partitionEntityList,
                                                         List<DictCountryEntity> countryEntityList,
                                                         List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                         List<CfgDeptRelationEntity> deptRelationList,
                                                         List<SysDepartmentEntity> deptList);

    Map<String, Object> syncAliExpressDataToSdyFieldHandler(SoB2cEntity soB2cEntity,
                                                            List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                            AliexpressDeliveryEntity aliexpressDeliveryEntity,
                                                            List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList,
                                                            AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity,
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
                                                            SoB2cReceiverEntity receiverEntity,
                                                            List<DictBasicEntity> omsAllDictList,
                                                            List<DictPartitionEntity> partitionEntityList,
                                                            List<DictCountryEntity> countryEntityList,
                                                            List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                            List<CfgDeptRelationEntity> deptRelationList,
                                                            List<SysDepartmentEntity> deptList);

    /**
     * 同步数帝云
     *
     * @param operateEnum
     * @param sourceType
     */
    void syncSdyOrderHandler(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> soB2cDetailEntityList, String operateEnum, String sourceType);


    void syncSdyCancelOrder(SoB2cEntity mainEntity, List<SoB2cDetailEntity> detailList, String code);


    Map<String, Pair<BigDecimal, BigDecimal>> convertAllDeliveryDetailPrice(List<SoB2cDeliveryDetailEntity> deliveryDetailList,
                                                          List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                          List<SkuVO> skuVOList,
                                                          List<BomChildrenSkuDTO> bomChildrenSkuDTOS
    );

    void hisSyncSelfDataToSdy(
            SoB2cEntity soB2cEntity,
            List<SoB2cDetailEntity> soB2cDetailEntityList,
            SoB2cDeliveryEntity soB2cDeliveryEntity,
            List<SoB2cDeliveryDetailEntity> allDeliveryDetail,
            SoB2cDeliveryDetailEntity soB2cDeliveryDetailEntity,
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
            SoB2cReceiverEntity receiverEntity,
            List<DictBasicEntity> omsAllDictList,
            List<DictPartitionEntity> partitionEntityList,
            List<DictCountryEntity> countryEntityList,
            List<DictGlobalAreaEntity> dictGlobalEntityList,
            List<CfgDeptRelationEntity> deptRelationList, List<SysDepartmentEntity> deptList);

    void hisSyncAliExpressDataToSdyFieldHandler(
            SoB2cEntity soB2cEntity,
            List<SoB2cDetailEntity> soB2cDetailEntityList,
            AliexpressDeliveryEntity aliexpressDeliveryEntity,
            List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList,
            AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity,
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
            SoB2cReceiverEntity receiverEntity,
            List<DictBasicEntity> omsAllDictList,
            List<DictPartitionEntity> partitionEntityList,
            List<DictCountryEntity> countryEntityList,
            List<DictGlobalAreaEntity> dictGlobalEntityList,
            List<CfgDeptRelationEntity> deptRelationList,
            List<SysDepartmentEntity> deptList);
}
