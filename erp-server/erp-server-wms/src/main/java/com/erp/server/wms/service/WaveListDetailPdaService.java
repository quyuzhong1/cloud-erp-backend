package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.model.wms.entity.WaveListDetailEntity;

/**
 * 波次详情（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
public interface WaveListDetailPdaService extends SuperService<WaveListDetailEntity> {
    Boolean hangUp(WaveListDetailPdaDTO.HangUpParamDTO hangUpDTO);

    WaveListDetailPdaDTO.ViewDTO startPicking(String waveId);

    WaveListDetailPdaDTO.FinishResultDTO finish(WaveListDetailPdaDTO.FinishParamDTO finishParamDTO);

    ApiResult scanSkuOrEanCode(String skuId, String code);
}
