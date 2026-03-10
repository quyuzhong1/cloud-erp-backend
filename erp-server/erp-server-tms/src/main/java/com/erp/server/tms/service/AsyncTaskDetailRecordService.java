package com.erp.server.tms.service;
import com.erp.model.tms.entity.AsyncTaskDetailRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.AsyncTaskDetailRecordDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 异步任务记录明细 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
public interface AsyncTaskDetailRecordService extends SuperService<AsyncTaskDetailRecordEntity> {

    void updateDetail(String taskDetailId, String status, String msg);
}
