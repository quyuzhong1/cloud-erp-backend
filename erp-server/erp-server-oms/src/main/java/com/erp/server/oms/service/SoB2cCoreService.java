package com.erp.server.oms.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.oms.dto.SoB2cCoreDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.wms.dto.SoOutstockDTO;

import java.util.List;

/**
 * sob2c额外类
 * @author will
 * @date 2025/4/24 20:07
 */
public interface SoB2cCoreService {
    /**
     * 查询重新出库数据
     * @author will
     * @date 2025/4/24 20:11
     * @param dto
     * @return List<ListRetryOutstockDTO>
     */
    List<SoB2cCoreDTO.ListRetryOutstockDTO> listRetryOutstock(BaseIdsDTO.IdsDTO dto);
    /**
     * 重新出库
     * @author will
     * @date 2025/4/24 20:28
     * @param list
     * @return Boolean
     */
    Boolean retryOutstock(List<SoB2cCoreDTO.RetryOutstockDTO> list);

    /**
     * 拆分b2c销售订单数据
     * @author will
     * @date 2025/4/28 10:22
     * @param mainEntity
     * @param generateB2cDTO
     * @return List<GenerateB2cDTO>
     */
    List<SoOutstockDTO.GenerateB2cDTO> splitB2cSoOutstock(SoB2cEntity mainEntity, SoOutstockDTO.GenerateB2cDTO generateB2cDTO);

    /**
     * 查询支付方式配置是否存在
     * @author will
     * @date 2025/5/30 10:56
     * @param entity
     * @return Boolean
     */
    Boolean listPayMethodSetting(SoB2cEntity entity);

    /**
     * 验证销售订单是否付款
     * @author will
     * @date 2025/5/30 15:54
     * @param entity
     * @return void
     */
    void checkPayMent(SoB2cEntity entity);

    void generateDeliveryAndOutStock(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> detailEntityList, SoB2cDTO.DeliveryWithNotOutboundDTO dto, SoB2cLogisticsEntity soB2cLogisticsEntity, SoB2cReceiverEntity soB2cReceiverEntity);
}
