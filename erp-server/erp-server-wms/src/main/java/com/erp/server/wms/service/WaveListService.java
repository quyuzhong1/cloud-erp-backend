package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.WaveListEntity;

import java.util.List;

/**
 * 波次拣货
 * @date 2024-06-28
 * @author tanmujin
 */
public interface WaveListService extends SuperService<WaveListEntity> {
    /**
     * 分页查询
     */
    PagingVO<WaveListDTO.ViewDTO> paging(PagingDTO<WaveListDTO.SearchParamDTO> pagingDTO);

    /**
     * tabList
     */
    List<WaveListDTO.TabDTO> tabList();
}
