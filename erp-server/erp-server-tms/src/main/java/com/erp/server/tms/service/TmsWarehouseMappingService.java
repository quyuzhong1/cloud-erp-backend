package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsWarehouseMappingDTO;
import com.erp.model.tms.entity.TmsWarehouseMappingEntity;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
public interface TmsWarehouseMappingService extends SuperService<TmsWarehouseMappingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsWarehouseMappingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    Boolean update(TmsWarehouseMappingDTO.UpdateDTO dto);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/3/19 11:39
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<TmsWarehouseMappingDTO.ListDTO> paging(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto);
}
