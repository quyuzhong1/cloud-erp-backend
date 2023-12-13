package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.common.business.service.SuperService;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * sku bom关系表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
public interface DmpSkuCostService extends SuperService<DmpSkuCostEntity> {

    /**
     * 同步采购单sku成本信息
     * @Author Luo_WG
     * @Date 2023/9/13 18:30
     * @return void
     **/
    void syncPurchaseOrderSkuCost(List<LocalDate> localDateList);

    /**
     * @description: 根据sku编码集合清洗成本数据
     * @author Will
     * @date: 2023/11/23 14:52
     * @param skuNoList
     */
    void cleanSkuCostBySKuNos(List<String> skuNoList);

    /**
     * 根据sku查询产品成本信息
     * @Author Luo_WG
     * @Date 2023/9/13 19:03
     * @param skuNoList
     * @return java.util.List<com.erp.model.dmp.entity.DmpSkuCostEntity>
     **/
    List<DmpSkuCostEntity> listDmpSkuCostBySkuNo(List<String> skuNoList);

    /**
     * @description: 根据skuId集合查询
     * @author Will
     * @date: 2023/11/23 12:04
     * @param skuIdList
     * @return List<DmpSkuCostEntity>
     */
    List<DmpSkuCostEntity> listBySkuIdList(List<String> skuIdList);

    /**
     * @description: 根据skuId集合查询缓存数据
     * @author Will
     * @date: 2023/11/23 12:29
     * @param skuNoList
     * @return List<DmpSkuCostEntity>
     */
    List<DmpSkuCostEntity> listRedisBySkuNoList(List<String> skuNoList);

}
