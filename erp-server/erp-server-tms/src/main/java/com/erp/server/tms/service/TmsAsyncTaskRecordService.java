package com.erp.server.tms.service;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.common.business.service.SuperService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    String addManualTask(String businessType, String json);

    String addAutoTask(String businessType, String json,String startTimeStr);

    void updateTask(String taskId, String status, String errorMsg);

    void terminateTaskTimeout(String taskId, String errorMsg);

    void updateTaskFinally(String taskId);

    PagingVO<TmsAsyncTaskRecordDTO.ListDTO> paging(PagingDTO<TmsAsyncTaskRecordDTO.PagingParamDTO> dto);

    List<TmsAsyncTaskRecordDTO.TabListDTO> tabList(PermissionsDTO dto);

    void exportList(TmsAsyncTaskRecordDTO.PagingParamDTO dto, HttpServletResponse response);

    PagingVO<TmsAsyncTaskRecordDTO.DetailListDTO> pagingError(PagingDTO<TmsAsyncTaskRecordDTO.PagingDetailParamDTO> dto);

    void exportError(TmsAsyncTaskRecordDTO.PagingDetailParamDTO dto, HttpServletResponse response);

    void updateStartTime(TmsAsyncTaskRecordDTO.UpdateDTO dto);

    BatchResultDTO retry(TmsAsyncTaskRecordEntity entity);

    BatchResultDTO errorRetry(TmsAsyncTaskRecordEntity entity);

    Boolean isExist(String businessType,  String startTimeStr);

    void updateTaskDetailFailure(String taskDetailId, Exception e);

    @Transactional(rollbackFor = Exception.class)
    void startTask();

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    void claimAndDispatch(TmsAsyncTaskRecordEntity entity);

    void genAutoTask();
}
