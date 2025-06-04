package com.erp.server.workflow.service;
import com.common.business.dto.FindUserDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.entity.ProcessDelegateEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ProcessDelegateDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 委托审批 服务类
 * </p>
 *
 * @author will
 * @since 2025-05-12
 */
public interface ProcessDelegateService extends SuperService<ProcessDelegateEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ProcessDelegateDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(ProcessDelegateDTO.UpdateDTO dto);

    /**
     * tab列表
     * @author will
     * @date 2025/5/12 16:08
     * @param dto
     * @return List<TabListDTO>
     */
    List<ProcessDelegateDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * 分页查询
     * @author will
     * @date 2025/5/12 16:09
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ProcessDelegateDTO.ListDTO> paging(PagingDTO<ProcessDelegateDTO.PagingParamDTO> dto);
    /**
     * 查询详情
     * @author will
     * @date 2025/5/12 16:11
     * @param id
     * @return ViewDTO
     */
    ProcessDelegateDTO.ViewDTO view(String id);
    /**
     * 终止委托
     * @author will
     * @date 2025/5/12 16:13
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO closeDelegate(String id);
    /**
     * 导出Excel
     * @author will
     * @date 2025/5/13 11:10
     * @param dto
     * @return void
     */
    void exportList(ProcessDelegateDTO.PagingParamDTO dto);
    /**
     * 根据流程定义id查询委托审批信息
     * @author will
     * @date 2025/5/19 17:47
     * @param processDefinitionId
     * @return List<ProcessDelegateEntity>
     */
    List<ProcessDelegateEntity> getByProcessDefinitionId(String processDefinitionId);
    /**
     * 查询未结束数据
     * @author will
     * @date 2025/5/27 16:53
     * @return List<ProcessDelegateEntity>
     */
    List<ProcessDelegateEntity> listNotEnded(LocalDateTime now);
    /**
     * xxjob更新
     * @author will
     * @date 2025/5/27 17:09
     * @param entity
     * @param now
     * @return void
     */
    void updateStatusJob(ProcessDelegateEntity entity, LocalDateTime now);
    /**
     * 根据登陆人id查询委托审批信息
     * @author will
     * @date 2025/5/29 19:48
     * @return List<FindUserDTO>
     */
    List<FindUserDTO> listStartUserId();
}
