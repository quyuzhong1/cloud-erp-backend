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
    void syncPurchaseOrderSkuCost(String flag, List<LocalDate> localDateList);

    /**
     * 根据sku查询产品成本信息
     * @Author Luo_WG
     * @Date 2023/9/13 19:03
     * @param skuNoList
     * @return java.util.List<com.erp.model.dmp.entity.DmpSkuCostEntity>
     **/
    List<DmpSkuCostEntity> listDmpSkuCostBySkuNo(List<String> skuNoList);
}
