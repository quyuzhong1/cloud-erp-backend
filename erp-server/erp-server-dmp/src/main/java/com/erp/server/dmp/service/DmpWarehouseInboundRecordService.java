package com.erp.server.dmp.service;

import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.server.dmp.entity.DmpWarehouseInboundRecordEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 海外仓上架记录表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-03-29
 */
public interface DmpWarehouseInboundRecordService extends SuperService<DmpWarehouseInboundRecordEntity> {

    /**
     *处理数据库业务
     * @param ext
     */
    Boolean checkOrder(GoodcangDTO.MessageDTO ext);

    /**
     * mq推送调拨上级数据处理
     * @param ext
     * @return
     */
    Boolean process(GoodcangDTO.MessageDTO ext);
}
