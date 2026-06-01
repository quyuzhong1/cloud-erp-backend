package com.erp.server.tms.service;
import com.common.business.dto.base.BatchResultDTO;
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

    /**
     * 新增手动任务（带方法类型，在同一 businessType 下区分不同方法）
     * 防重键为 businessType + methodType + dataJson
     *
     * @param detailCount 预期明细数量（创建时直接写入，省去二次更新）
     * @return 创建成功的任务记录；命中防重或保存失败时返回 null
     */
    TmsAsyncTaskRecordEntity addManualTask(String businessType, String methodType, Integer detailCount, String json);

    /**
     * 新增自动任务（带方法类型）
     */
    String addAutoTask(String businessType, String methodType, String json, String startTimeStr);

    void updateTask(String taskId, String status, String errorMsg);

    /**
     * 任务未执行即失败收尾：FINISH + errorData + errorCount=1，便于任务列表识别异常结束
     */
    void finishTaskWithError(String taskId, String errorMsg);

    /**
     * 主表驱动任务正常收尾：FINISH + endTime，保留主表已有 detailCount/errorCount
     */
    void finishTaskOnMainRecord(String taskId);

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

    /**
     * 实现类需保证事务边界。
     */
    void startTask();


    void genAutoTask();
}
