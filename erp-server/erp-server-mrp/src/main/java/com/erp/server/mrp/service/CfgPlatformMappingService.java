package com.erp.server.mrp.service;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgPlatformMappingDTO;

/**
 * <p>
 * 平台映射表 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
public interface CfgPlatformMappingService extends SuperService<CfgPlatformMappingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgPlatformMappingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    Boolean update(CfgPlatformMappingDTO.UpdateDTO dto);

    /**
     * 远程下拉
     * @author will
     * @date 2024/8/29 17:17
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<CfgPlatformMappingDTO.ListDTO> selectPaging(PagingDTO<CfgPlatformMappingDTO.SelectDTO> dto);
}
