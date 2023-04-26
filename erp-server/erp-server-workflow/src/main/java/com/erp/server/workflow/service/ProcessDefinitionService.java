package com.erp.server.workflow.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.common.business.service.SuperService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    /**
     * 流程发布
     * @param dto
     * @return
     */
    ProcessDTO.DeployResultDTO deploy(ProcessDTO.DeployDTO dto);

    /**
     * 流程定义复制
     * @param dto
     * @return
     */
    ProcessDefinitionDTO.CopyResultDTO copy(ProcessDefinitionDTO.CopyDTO dto);

    /**
     * 流程定义导出
     * @param dto
     * @return
     */
    Boolean exportExcel(ProcessDefinitionDTO.QueryExportDTO dto, HttpServletResponse response);

    /**
     * 删除流程定义
     * @param ids
     * @return
     */
    boolean deleteByIds(List<String> ids);
}
