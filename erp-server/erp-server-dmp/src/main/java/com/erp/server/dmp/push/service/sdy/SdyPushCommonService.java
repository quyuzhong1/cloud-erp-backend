package com.erp.server.dmp.push.service.sdy;

import com.erp.model.wms.dto.ShudiyunB2cOrderDTO;

import java.util.List;

public interface SdyPushCommonService {
    void executeConsumer(List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList);
}
