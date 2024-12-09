package com.erp.server.wms.kingdee;

import java.util.List;
import java.util.Map;

import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;

/**
 * 销售出库单
 * @Author Luo_WG
 * @Date 2023/5/31 16:41
 **/
public interface SyncKingdeeSoOutstockService {
    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    DmpPushTaskEntity syncDataToKingdee(SoOutstockEntity entity, String operate);
    
    Map<String , Object> newSyncDataToKingdee(SoOutstockEntity entity, String operate);

    /**
     * 发送消息同步金蝶
     * @description
     * @param entity
     * @param
     * @author Lambda
     * @return
     * @create 2023-12-27 16:50
     */
    DmpPushTaskEntity syncB2cDataToKingdee(SoOutstockEntity entity, String operate);
    
    Map<String , Object> newSyncB2cDataToKingdee(SoOutstockEntity entity, String operate);

    /**
     *  同步旺店通数据到金蝶
     */
    DmpPushTaskEntity syncWdtDataToKingdee(SoOutstockEntity entity, String operate);
    
    Map<String , Object> newSyncWdtDataToKingdee(SoOutstockEntity entity, String operate);
    /**
     * 推送订单到mq
     *
     * @param entity
     * @param syncOperate
     */
    void syncOrderToDmp(SoOutstockEntity entity, String syncOperate);

    /**
     * 同步数帝云字段映射处理
     */
    Map<String, Object> syncDataToSdyFieldHandler(SoOutstockEntity entity,
                                                  SoOutstockDetailEntity soOutstockDetailEntity,
                                                  String operate,
                                                  List<CurrencyDTO.ViewDTO> currencyList,
                                                  List<ShopInfoEntity> shopInfoList,
                                                  List<CustomerInfoEntity> customerInfoList,
                                                  List<BaseIdDTO.CodeDTO> companyEntities,
                                                  List<SkuVO> skuVOList,
                                                  List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                  List<ProductDetailEntity> parentSkuList,
                                                  List<SoB2cEntity> soB2cEntities,
                                                  List<SoInfoEntity> soInfoEntities,
                                                  List<DictBasicEntity> dictBasicEntityList);

    /**
     * 同步数帝云
     */
    void syncDataToSdy(SoOutstockEntity entity, List<SoOutstockDetailEntity> soOutstockDetailEntityList, String operate);

    void syncDataToSdy(SoOutstockEntity entityList, List<SoOutstockDetailEntity> detailEntities, String operate, List<CurrencyDTO.ViewDTO> currencyList, List<ShopInfoEntity> shopInfoList, List<CustomerInfoEntity> customerInfoList, List<BaseIdDTO.CodeDTO> companyEntities, List<SkuVO> skuVOList, List<BomChildrenSkuDTO> bomChildrenSkuDTOS, List<ProductDetailEntity> parentSkuList, List<SoB2cEntity> soB2cEntities, List<SoInfoEntity> soInfoEntities, List<DictBasicEntity> dictBasicEntityList);
}
