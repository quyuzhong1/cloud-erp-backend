package com.erp.server.workflow.service;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.entity.ApproveSyncRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ApproveSyncRecordDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * ERP审批同步-通知配置 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
public interface ApproveSyncRecordService extends SuperService<ApproveSyncRecordEntity> {


    List<ApproveSyncRecordDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<ApproveSyncRecordDTO.ListDTO> paging(PagingDTO<ApproveSyncRecordDTO.PagingParamDTO> dto);

    void exportList(ApproveSyncRecordDTO.PagingParamDTO dto, HttpServletResponse response);

    BatchResultDTO repush(String id);

    void insertBatch(List<ApproveSyncRecordEntity> list);
}
