package com.erp.server.tms.service;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.entity.FirstMileChangeRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;

import java.util.List;

/**
 * <p>
 * 头程调整记录 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-05-12
 */
public interface FirstMileChangeRecordService extends SuperService<FirstMileChangeRecordEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileChangeRecordDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(FirstMileChangeRecordDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author zdy
     * @date: 2025-05-12
     * @param dto
     * @return
     */
    PagingVO<FirstMileChangeRecordDTO.PagingVO> paging(PagingDTO<FirstMileChangeRecordDTO.PagingParamDTO> dto);

    /**
     * 导出头程调整记录
     * @param dto
     */
    void exportList(FirstMileChangeRecordDTO.PagingParamDTO dto);

    /**
     * 校验单产品重量是否一致
     * @param dtoValidList
     */
    List<BatchResultDTO> checkSameDimension(List<FirstMileWeightAllocationDTO.ProductWeightDTO> dtoValidList);
    /**
     * 修改商品重量
     * @param dtoValidList
     * @return
     * */
    void saveProductWeight(List<FirstMileWeightAllocationDTO.ProductWeightDTO> dtoValidList);
    void saveProductWeightByEntity(List<FirstMileChangeRecordEntity> list);
    /**
     * 根据参数获取最新调整记录
     *
     * @param sourceType    调整类型
     * @param deliveryId    发货单id
     * @param businessCode  业务单号
     * @param skuId         数大臣sku
     * @param categoryField 调整字段
     * @param sourceId      调整来源id
     * @param boxId         箱子id
     * @param platformSkuNo
     * @return
     */
    FirstMileChangeRecordEntity getProductWeightByParams(String sourceType, String deliveryId, String businessCode, String skuId, String categoryField, String sourceId, String boxId, String platformSkuNo);

    /**
     * 修改产品尺寸校验
     * @param dtoValidList
     * @return
     */
    List<BatchResultDTO> checkPackageSameDimension(List<FirstMileWeightAllocationDTO.PackageSizeDTO> dtoValidList);

    /**
     * 修改产品尺寸
     * @param dtoValidList
     */
    void savePackageWeight(List<FirstMileWeightAllocationDTO.PackageSizeDTO> dtoValidList);
    void savePackageByEntity(List<FirstMileChangeRecordEntity> entityList);
    /**
     * 根据参数获取最新出库重量调整记录
     *
     * @param sourceType    调整类型
     * @param deliveryId    发货单id
     * @param businessCode  业务单号
     * @param categoryField 调整字段
     * @param boxId 箱子id
     * @return
     */
    FirstMileChangeRecordEntity getOutStockWeightByParams(String sourceType, String deliveryId, String businessCode, String categoryField, String boxId);

    /**
     * 校验头程费用分摊参数
     * @param dtoValidList
     * @return
     */
    List<BatchResultDTO> checkCostAllocationSameDimension(ValidList<FirstMileCostAllocationDTO.CostAllocationDTO> dtoValidList);

    /**
     *
     * @param dtoValidList
     */
    void saveCostAllocation(ValidList<FirstMileCostAllocationDTO.CostAllocationDTO> dtoValidList);

    void saveCostByEntity(List<FirstMileChangeRecordEntity> entityList);

    /**
     * 根据参数获取费用分摊最新调整记录
     *
     * @param sourceType     调整类型
     * @param deliveryId     发货单id
     * @param businessCode   业务单号
     * @param categoryField  调整字段
     * @param skuId          数大臣sku
     * @param platformSkuNo  平台SKU
     * @param category       调整分类
     * @param reportPeriodId 核算区间id
     * @param id            分摊记录id
     * @return
     */
    FirstMileChangeRecordEntity getCostAllocationByParams(String sourceType, String deliveryId, String businessCode, String categoryField, String skuId, String platformSkuNo, String category, String reportPeriodId, String id);

    void updateCostIsLatest(String sourceType, String businessCode, String sourceCode, String logisticsBillId, String skuId, String feeType, String categoryField);

    FirstMileChangeRecordEntity getCostAllocationWeightByParams(String sourceType, String deliveryId, String businessCode, String categoryField, String skuId, String platformSkuNo);
}
