package com.erp.server.sys.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CfgTableArchiveDTO;
import com.erp.model.sys.entity.CfgTableArchiveEntity;

/**
 * <p>
 * 归档配置表 服务类
 * </p>
 */
public interface CfgTableArchiveService extends SuperService<CfgTableArchiveEntity> {

    PagingVO<CfgTableArchiveDTO.ListDTO> paging(PagingDTO<CfgTableArchiveDTO.SearchParamDTO> pagingDTO);

    Boolean add(CfgTableArchiveDTO.AddDTO dto);

    Boolean update(CfgTableArchiveDTO.UpdateDTO dto);

    Boolean delete(BaseIdsDTO.IdsDTO idsDTO);

    /**
     * 执行单条归档配置
     */
    int executeArchive(String id);

    /**
     * 执行所有归档配置
     */
    int executeAllArchive();
}
