package com.erp.server.tms.service;
import java.util.List;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.TransferDeclareCostAllocationEntity;
import com.erp.model.tms.entity.TransferDeclareCostAllocationMainEntity;

/**
 * <p>
 * 中转费用分摊 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
 */
public interface TransferDeclareCostAllocationService extends SuperService<TransferDeclareCostAllocationEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareCostAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareCostAllocationDTO.UpdateDTO dto);

    List<TransferDeclareCostAllocationDTO.TabListDTO> tabList(PermissionsDTO dto);
    
    PagingVO<TransferDeclareCostAllocationDTO.ListDTO> paging(PagingDTO<TransferDeclareCostAllocationDTO.PagingParamDTO> dto);
    
    BatchResultDTO updateReportStatus(String id , String reportDate , String reportStatus);

    /**
     * 按核算月份异步批量更新核算状态
     */
    BatchResultDTO asyncUpdateReportStatus(TransferDeclareCostAllocationDTO.UpdateStatusDTO dto);

    /**
     * MQ 消费：游标分批批量更新核算状态
     */
    void pushUpdateReportStatus(TmsAsyncTaskRecordDTO.PushParamsDTO dto);

    /** MQ 消费：下推中转费用分摊（含任务明细初始化） */
    void pushAllocation(TmsAsyncTaskRecordDTO.PushParamsDTO dto);

    BatchResultDTO asyncReAllocation(FirstMileCostAllocationDTO.ResetIdsDTO dto);

    void pushReAllocation(TmsAsyncTaskRecordDTO.PushParamsDTO dto);

    BatchResultDTO asyncDelete(FirstMileCostAllocationDTO.ResetIdsDTO dto);

    void pushDelete(TmsAsyncTaskRecordDTO.PushParamsDTO dto);
    
    BatchResultDTO reAllocation(String id);

    BatchResultDTO reAllocation(TransferDeclareCostAllocationMainEntity entity);
    
    BatchResultDTO delete(String id);
    
    BatchResultDTO pushBigTable(String id);
    
    Boolean exportExcel(TransferDeclareCostAllocationDTO.PagingParamDTO dto);

    List<TransferDeclareCostAllocationEntity> listByReportPeriodStr(String reportPeriodStr, String reportStatus);
}
