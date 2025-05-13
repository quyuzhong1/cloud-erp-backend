package com.erp.server.workflow.service;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.entity.ProcessDelegateEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ProcessDelegateDTO;

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
}
