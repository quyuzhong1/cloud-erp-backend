package com.erp.server.tms.service;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileSkuCostAllocationDetailDTO;

import java.util.List;

/**
 * <p>
 * 头程费用SKU分摊明细 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
public interface FirstMileSkuCostAllocationDetailService extends SuperService<FirstMileSkuCostAllocationDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileSkuCostAllocationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    Boolean update(FirstMileSkuCostAllocationDetailDTO.UpdateDTO dto);

    /**
     * 根据主表id删除明细
     * @param id
     */
    void removeByMainId(String id);

    /**
     * 根据主表id获取明细记录
     * @param mainIds
     * @return
     */
    List<FirstMileSkuCostAllocationDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据明细id修改期末在途费用
     * @param detailId
     * @param newEndPeriodTransitCost
     */
    void updateEndPeriodTransitCost(String detailId, String newEndPeriodTransitCost);

    /**
     * 根据明细id修改期末暂估费用
     * @param detailId
     * @param newEndPeriodEstimatedCost
     */
    void updateEndPeriodEstimatedCost(String detailId, String newEndPeriodEstimatedCost);

    List<FirstMileSkuCostAllocationDetailEntity> listBySourceCodeList(List<String> businessCodeList, List<String> sourceCodeList, List<String> transportNoList);

    void updateDetailRemark(String detailId, String newDetailRemark);

    List<FirstMileSkuCostAllocationDetailEntity> listByReportMonth(String sourceId, String businessCode, String transportNo, String skuId, String platformSkuNo, String reportPeriodId);
}
