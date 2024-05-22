package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;

/**
 * b2c订单拆分操作服务类
 */
public interface SoB2cSplitService extends SuperService<SoB2cEntity> {

    List<BatchResultDTO> bomSplitAndSave(List<String> ids);

    List<BatchResultDTO> bomRestoreAndSave(List<String> ids);

    List<SoB2cEntity> listRefBomSplit(String detailId);
}
