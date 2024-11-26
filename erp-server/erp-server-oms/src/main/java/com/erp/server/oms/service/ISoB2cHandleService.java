package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;

/**
 * B2c订单处理
 **/
public interface ISoB2cHandleService<T> {

    /**
     * 规则处理
     */
    Boolean handleRule(SoB2cEntity mainEntity);

    /**
     * 处理生成销售单
     */
    Boolean handleSoOutStock(PlatformOrderDTO dto, SoB2cDTO.PullOrderResultDTO resultDTO, SoB2cEntity mainEntity);

    /**
     * 转换新中台刷新订单请求参数
     */
    List<DmpInoutDTO.CreateInputDTO> convertCreateInputDTOList(List<SoB2cEntity> orderEntityList);
}
