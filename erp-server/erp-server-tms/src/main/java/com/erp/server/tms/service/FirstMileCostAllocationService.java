package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 头程费用分摊 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
public interface FirstMileCostAllocationService extends SuperService<FirstMileCostAllocationEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileCostAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    Boolean update(FirstMileCostAllocationDTO.UpdateDTO dto);

    /**
     * 分页统计
     * @param dto
     * @return
     */
    List<FirstMileCostAllocationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<FirstMileCostAllocationDTO.PagingVO> paging(PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> dto);

    /**
     * 删除
     * @param entity
     * @return
     */
    BatchResultDTO delete(FirstMileCostAllocationEntity entity);

    /**
     * 导出excel
     * @param dto
     */
    void exportList(FirstMileCostAllocationDTO.PagingParamDTO dto);

    /**
     * 计算分摊费用
     *
     * @param entity
     * @param firstMileDeliveryEntity
     * @param firstMileDeliveryDetailEntityList
     * @return
     */
    BatchResultDTO calcAllocatedCost(FirstMileCostAllocationEntity entity, FirstMileDeliveryEntity firstMileDeliveryEntity,List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList);

    /**
     * 根据发货单获取全部费用分摊记录
     *
     * @param sourceIds
     * @param reportPeriodId
     * @return
     */
    List<FirstMileCostAllocationEntity> listBySourceIds(List<String> sourceIds, String reportPeriodId, String supplierId, String supplierType);

    /**
     * 自动生成费用分摊
     *
     * @param reportPeriodMonth
     * @param sourceId
     */
    void autoGenerateFirstMileCostAllocation(LocalDate reportPeriodMonth, String sourceId);

    /**
     * 获取物流单对应的最新核算期间
     */
    List<FirstMileCostAllocationDTO.LastedAllocMonthDTO> listLastedAllocationMonth(List<String> logisticsBillIds);

    /**
     * 更新状态
     * @param entity
     * @param status
     * @param accountPeriod
     * @return
     */
    BatchResultDTO updateStatus(FirstMileCostAllocationEntity entity, String status, String accountPeriod);

    /**
     * 根据对账单id查询费用分摊记录
     * @param reconciliationIds
     * @return
     */
    List<FirstMileCostAllocationEntity> listByReconciliationIds(List<String> reconciliationIds);

    /**
     * 根据物流id查询分摊表
     * @param logisticsBillIds
     * @return
     */
    List<FirstMileCostAllocationEntity> listByLogisticsBillIds(List<String> logisticsBillIds);

    /**
     * 查看费用分摊详情
     * @param ids
     * @return
     */
    List<FirstMileCostAllocationDTO.PagingVO> viewCostAllocation(List<String> ids);

    /**
     * 根据业务信息查询分摊记录
     * @param sourceId
     * @param businessCode
     * @param reportMonth
     * @return
     */
    List<FirstMileCostAllocationDTO.DetailDTO> getRecordBySourceIdAndCode(String sourceId, String businessCode, LocalDate reportMonth);
    /**
     * 根据skuId和业务类型查询头程费用分摊记录
     * @param skuId
     * @param businessCode
     * @param reportMonth
     * @return
     */
    List<FirstMileCostAllocationDTO.DetailDTO> getRecordBySkuIdAndCode(String skuId, String businessCode, LocalDate reportMonth);

    /**
     * 根据账期查询头程费用分摊记录
     * @param reportPeriodStr
     * @param reportStatus
     * @return
     */
    List<FirstMileCostAllocationEntity> listByReportPeriodStr(String reportPeriodStr, String reportStatus);

    void downloadTemplate(HttpServletResponse response);

    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);

    void asyncResetAllocatedCost(List<FirstMileCostAllocationEntity> entityList, List<FirstMileDeliveryEntity> firstMileDeliveryEntityList, List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList);
}
