package com.erp.server.dmp.service;

import com.common.business.enums.BusinessTypeEnum;
import com.erp.model.dmp.DmpPullOtherOutStockDTO;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;

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


    /**
     * 检查并重推生成其他出库单
     *
     * @author Jim
     * {@code @date:} 2024-03-12
     */
    Boolean checkAndSendOtherOutStock(DmpPullOtherOutStockDTO resultDTO);


    /**
     * 单处理平台仓B2C销售出库单
     */
    void singleHandlerConsumer(PlatformAmazonFulfilledShipmentsDTO currentDTO, String tableName, String platform, BusinessTypeEnum businessType, String topic, String tag);

}
