package com.erp.server.workflow.service;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.entity.CfgApproveSyncEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.lark.oapi.service.approval.v4.model.CreateExternalInstanceReq;
import com.lark.oapi.service.approval.v4.model.I18nResource;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * ERP审批同步配置 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
public interface CfgApproveSyncService extends SuperService<CfgApproveSyncEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgApproveSyncDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(CfgApproveSyncDTO.UpdateDTO dto);


    List<CfgApproveSyncDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<CfgApproveSyncDTO.ListDTO> paging(PagingDTO<CfgApproveSyncDTO.PagingParamDTO> dto);

    BatchResultDTO delete(String id);

    BatchResultDTO enable(String id,Boolean enableStatus);

    void exportList(CfgApproveSyncDTO.PagingParamDTO dto, HttpServletResponse response);

    CfgApproveSyncDTO.ViewDTO view(String id);

    List<CfgApproveSyncEntity> getByBusinessType(List<String> businessTypes);

    //构建I18nResource数组
    I18nResource[] mapToI18nResouceArray(Map<String, String> values);

    void cleanFeishuTest();
}
