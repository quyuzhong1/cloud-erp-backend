package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OverseasInventoryAgeDetailDTO;
import com.erp.model.wms.entity.OverseasInventoryAgeDetailEntity;

import java.util.List;

/**
 * <p>
 * 海外仓库存库龄明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2024-12-06
 */
public interface OverseasInventoryAgeDetailService extends SuperService<OverseasInventoryAgeDetailEntity> {

        List<OverseasInventoryAgeDetailDTO.AgeRangeViewDTO> getAgeRangeViewByMainIds(List<String> mainIds);

        PagingVO<OverseasInventoryAgeDetailDTO.ListDTO> pagingSelect(PagingDTO<OverseasInventoryAgeDetailDTO.PagingParamDTO> dto);
}
