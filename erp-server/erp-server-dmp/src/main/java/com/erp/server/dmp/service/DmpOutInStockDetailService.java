package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpOutInStockDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;


/**
 * <p>
 * 手工出入库详情表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
 */
public interface DmpOutInStockDetailService extends SuperService<DmpOutInStockDetailEntity> {

    /**
     * 根据主单id查询
     * @param mainId
     * @return
     */
    List<DmpOutInStockDetailEntity> findByMainId(String mainId);

}
