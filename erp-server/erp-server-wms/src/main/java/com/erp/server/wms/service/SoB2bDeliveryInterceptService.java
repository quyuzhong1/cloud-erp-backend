package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoB2bDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2bDeliveryInterceptEntity;

import java.util.List;

/**
 * <p>
 * b2b发货拦截单 服务类
 * </p>
 *
 * @author Codex
 */
public interface SoB2bDeliveryInterceptService extends SuperService<SoB2bDeliveryInterceptEntity> {

    BaseResultDTO.AddDTO add(SoB2bDeliveryInterceptDTO.AddDTO dto);

    List<SoB2bDeliveryInterceptDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<SoB2bDeliveryInterceptDTO.ListDTO> paging(PagingDTO<SoB2bDeliveryInterceptDTO.PagingParamDTO> dto);

    SoB2bDeliveryInterceptDTO.ViewDTO view(String id);

    SoB2bDeliveryInterceptEntity getLatestBySourceId(String sourceId);

    void handleResultBySourceId(String sourceId, String handleResult, String handleRemark, String transportNo, String soOutstockCode);
}
