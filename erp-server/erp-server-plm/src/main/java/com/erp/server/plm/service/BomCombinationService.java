package com.erp.server.plm.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.BomCombinationDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 组合产品业务层
 * @date 2023/8/16 10:00
 */
public interface BomCombinationService {
    /**
     * @description: 列表查询
     * @author Will
     * @date: 2023/8/16 10:02
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<BomCombinationDTO.ListDTO> paging(PagingDTO<BomCombinationDTO.SearchParamDTO> dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/8/16 10:08
     * @param dto
     * @return Boolean
     */
    Boolean add(BomCombinationDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/8/16 10:18
     * @param dto
     * @return Boolean
     */
    Boolean update(BomCombinationDTO.UpdateDTO dto);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/8/16 10:19
     * @param dto
     * @return Boolean
     */
    Boolean view(BaseIdDTO dto);
}
