package com.common.business.service;

import com.common.business.dto.WdtSearchHandelDetailDTO;

import java.util.List;

/**
 * 旺店通虚拟仓库存服务
 * @author will
 * @date 2025/12/19 18:12
 */
public interface WdtVirtualInventoryService {
    /**
     * 旺店通库存查询
     * @author will
     * @date 2025/12/19 18:12
     * @param detailDTO
     * @return List<SearchVirtualInventoryDTO>
     */
    List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> searchVirtualInventory(WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO detailDTO);

}
