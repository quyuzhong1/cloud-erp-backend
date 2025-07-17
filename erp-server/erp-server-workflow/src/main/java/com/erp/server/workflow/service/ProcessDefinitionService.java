package com.erp.server.workflow.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;

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
    boolean addOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto);

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
     *
     * @param dto
     * @return
     */
    Boolean exportExcel(ProcessDefinitionDTO.QueryExportDTO dto);

    /**
     * 删除流程定义
     * @param id
     * @return
     */
    BatchResultDTO deleteByIds(String id,Integer processVersion,Boolean isValidate);

    PagingVO<ProcessDefinitionDTO.ExportDTO> exportProcessDefinition(PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto);

    List<ProcessDefinitionDTO.DropDTO> getProcessDefinition(String businessKey);
    /**
     * 更新启禁用状态
     * @author will
     * @date 2025/5/15 15:56
     * @param disableDTO
     * @return BatchResultDTO
     */
    BatchResultDTO updateDisabled(ProcessDefinitionDTO.DisableDTO disableDTO);
    /**
     * 查询tab数量
     * @author will
     * @date 2025/5/15 16:03
     * @param dto
     * @return List<TabListDTO>
     */
    List<ProcessDefinitionDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     *  高级查询下拉
     * @param type
     * @return
     */
    List<ProcessDefinitionDTO.DropDownDTO> dropDown(String type);

    /**
     * 界面下拉
     * @return
     */
    List<ProcessDefinitionDTO.DropDownDTO> proDropDown();

    /**
     * 变更流程
     * @author will
     * @date 2025/5/23 19:04
     * @param dto
     * @return Boolean
     */
    Boolean changeProcess(ProcessDefinitionDTO.ProcessChangeDTO dto);
    /**
     * 查询已发布流程设计
     * @author will
     * @date 2025/5/27 16:38
     * @param id
     * @return ProcessDefinitionEntity
     */
    ProcessDefinitionEntity getIsDeployEntityById(String id);

    /**
     * 根据id和版本查询
     * @author will
     * @date 2025/5/30 16:35
     * @param id
     * @param processVersion
     * @return ProcessDefinitionEntity
     */
    ProcessDefinitionEntity getProcessVersionEntity(String id,Integer processVersion);
}
