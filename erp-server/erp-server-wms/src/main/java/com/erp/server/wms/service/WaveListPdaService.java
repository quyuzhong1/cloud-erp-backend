package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.model.wms.dto.WaveListPdaDTO;
import com.erp.model.wms.entity.WaveListEntity;

import java.util.List;

/**
 * 波次列表（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
public interface WaveListPdaService extends SuperService<WaveListEntity> {

    PagingVO<WaveListPdaDTO.ViewDTO> paging(PagingDTO<WaveListDTO.SearchParamDTO> pagingDTO);

    WaveListPdaDTO.WaveBasicInfoDTO waveInfo(String waveId);

    List<WaveListPdaDTO.ProductDetailDTO> productDetail(String waveId);

    ApiResult<?> bindPickingCart(WaveListPdaDTO.BindPickingCartDTO bindDTO);

    ApiResult<?> exitPicking(WaveListDetailPdaDTO.ExitPickingDTO exitDTO);

    /**
     * tabList
     */
    List<WaveListDTO.TabDTO> tabList();
}
