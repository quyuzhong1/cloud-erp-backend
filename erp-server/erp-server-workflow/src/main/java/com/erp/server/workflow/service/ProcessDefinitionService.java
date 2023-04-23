package com.erp.server.workflow.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
public interface ProcessDefinitionService extends SuperService<ProcessDefinitionEntity> {

    /**
     * 新增或修改流程定义
     * @param dto
     * @return
     */
    boolean saveOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto);

    /**
     * 分页查询流程定义
     * @param dto
     * @return
     */
    PagingVO<ProcessDefinitionDTO.ListDTO> paging(PagingDTO<ProcessDefinitionDTO.QueryDTO> dto);
}
