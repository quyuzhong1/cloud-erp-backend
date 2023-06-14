package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.common.business.service.SuperService;
import com.erp.model.dmp.mabang.ComboSkuInfoEntity;


/**
 * <p>
 * sku bom关系表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
public interface DmpBomService extends SuperService<DmpBomEntity> {


    /**
     * 检查Bom
     * @param ext
     */
    void checkOrder(ComboSkuInfoEntity ext);
}
