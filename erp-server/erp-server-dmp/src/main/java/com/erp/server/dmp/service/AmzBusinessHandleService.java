package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.dmp.entity.AmzReportInfoEntity;

/**
 * <p>
 * 亚马逊处理 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
public interface AmzBusinessHandleService {

    /**
     * 检查并重推生成销售出库单
     *
     * @author Jim
     * {@code @date:} 2024-03-12
     */
    Boolean checkAndSendSoOutStock(DmpPullSoOutStockDTO dto);
}
