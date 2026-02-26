package com.erp.server.tms.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.common.business.service.SuperService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 异步任务记录 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
public interface TmsAsyncTaskRecordService extends SuperService<TmsAsyncTaskRecordEntity> {

    String addTask(String businessType, String json);

    void updateTask(String taskId,String status, String errorMsg);

    void updateTaskFinally(String taskId);

    PagingVO<TmsAsyncTaskRecordDTO.ListDTO> paging(PagingDTO<TmsAsyncTaskRecordDTO.PagingParamDTO> dto);

    List<TmsAsyncTaskRecordDTO.TabListDTO> tabList(PermissionsDTO dto);

    void exportList(TmsAsyncTaskRecordDTO.PagingParamDTO dto, HttpServletResponse response);

    PagingVO<TmsAsyncTaskRecordDTO.DetailListDTO> pagingError(PagingDTO<TmsAsyncTaskRecordDTO.PagingDetailParamDTO> dto);

    void exportError(TmsAsyncTaskRecordDTO.PagingDetailParamDTO dto, HttpServletResponse response);

    void updateStartTime(TmsAsyncTaskRecordDTO.UpdateDTO dto);
}
