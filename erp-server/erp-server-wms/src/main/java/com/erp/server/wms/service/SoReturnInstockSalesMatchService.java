package com.erp.server.wms.service;

import com.common.business.dto.PlatformReturnInstockDTO;
import com.erp.model.oms.entity.SoB2cEntity;

/**
 * 退货入库单关联原始销售订单（含多渠道 WFHD 回溯）
 */
public interface SoReturnInstockSalesMatchService {

    /**
     * 从退货 MQ 报文中解析 WFHD/多渠道关联键，回溯原始 B2C 销售订单。
     *
     * @param dto 平台退货入库报文
     * @return 原始销售订单；未命中返回 null
     */
    SoB2cEntity matchOriginalSoB2c(PlatformReturnInstockDTO dto);
}
