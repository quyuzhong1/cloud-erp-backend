package com.erp.server.oms.kingdee;

import java.util.List;
import java.util.Map;

import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.*;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoService

 * @Date 2023-05-30 11:46
 * @Created by yl
 */
public interface SyncKingdeeSoService {

    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(SoInfoEntity entity, String operate);

    void syncOrderToDmp(SoInfoEntity entity, String operate);

    Map<String, Object> newSyncDataToKingdee(SoInfoEntity entity, String operate);

    /**
     * 同步数帝云字段映射处理
     */
    Map<String, Object> syncDataToSdyFieldHandler(SoInfoEntity soInfoEntity,
                                                  SoDetailEntity soDetailEntity,
                                                  List<SoDetailEntity> detailEntityList,
                                                  String operate,
                                                  List<SkuVO> skuVOList,
                                                  List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                  List<ProductDetailEntity> parentSkuList,
                                                  List<CustomerInfoEntity> customerInfoEntities,
                                                  List<BaseIdDTO.CodeDTO> companyEntities,
                                                  List<CurrencyDTO.ViewDTO> currencyList,
                                                  List<SoChangeDetailEntity> soChangeDetailEntityList,
                                                  List<DictBasicEntity> omsAllDictList,
                                                  List<DictPartitionEntity> partitionEntityList,
                                                  List<DictCountryEntity> countryEntityList,
                                                  List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                  List<CfgDeptRelationEntity> deptRelationList, List<SysDepartmentEntity> deptList);

    /**
     * 同步数帝云
     * @param view
     * @param soDetailEntityList
     * @param operate
     */
    void syncDataToSdy(SoInfoDTO.ViewDTO view, List<SoDetailEntity> soDetailEntityList, String operate);

    void syncDataToSdy(SoInfoEntity soInfoEntity,
                       List<SoDetailEntity> detailEntityList,
                       String operate,
                       List<SkuVO> skuVOList,
                       List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                       List<ProductDetailEntity> parentSkuList,
                       List<CustomerInfoEntity> customerInfoEntities,
                       List<BaseIdDTO.CodeDTO> companyEntities,
                       List<CurrencyDTO.ViewDTO> currencyList,
                       List<SoChangeDetailEntity> soChangeDetailEntities,
                       List<DictBasicEntity> omsAllDictList,
                       List<DictPartitionEntity> partitionEntityList,
                       List<DictCountryEntity> countryEntityList,
                       List<DictGlobalAreaEntity> dictGlobalEntityList,
                       List<CfgDeptRelationEntity> deptRelationList,
                       List<SysDepartmentEntity> deptList
    );

}