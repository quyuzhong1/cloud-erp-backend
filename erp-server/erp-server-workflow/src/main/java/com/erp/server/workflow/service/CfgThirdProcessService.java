package com.erp.server.workflow.service;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgThirdProcessDTO;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 三方审批生成 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-23
 */
public interface CfgThirdProcessService extends SuperService<CfgThirdProcessEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgThirdProcessDTO.AddDTO dto);

    /**
    * 修改
    * @author hcg
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    Boolean update(CfgThirdProcessDTO.UpdateDTO dto);

    /**
     * 分页
     * @author hcg
     * @date: 2025-05-23
     * @param dto
     * @return
     */
    PagingVO<CfgThirdProcessDTO.ListDTO> paging(PagingDTO<CfgThirdProcessDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @author hcg
     * @date: 2025-05-23
     * @param id
     * @return
     */
    CfgThirdProcessDTO.ViewDTO view(String id);

    /**
     * 批量启用、停用
     * @author hcg
     * @date: 2025-05-23
     * @param enableStatus
     * @return
     */
    BatchResultDTO enable(String id, @NotNull(message = "启用状态不能为空") Boolean enableStatus);
    /**
     * 导出
     * @author hcg
     * @date: 2025-05-23
     * @param dto
     * @return
     */
    void exportList(CfgThirdProcessDTO.PagingParamDTO dto, HttpServletResponse response);
    /**
     * 删除
     * @author hcg
     * @date: 2025-05-23
     * @param id
     * @return
     */
    BatchResultDTO delete(String id);

    List<CfgThirdProcessDTO.TabListDTO> tabList(PermissionsDTO dto);
}
