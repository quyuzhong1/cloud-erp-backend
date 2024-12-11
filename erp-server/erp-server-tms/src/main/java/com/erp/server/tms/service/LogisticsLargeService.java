package com.erp.server.tms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.*;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsLargeDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;

import java.util.List;

/**
 * <p>
 * 物流大表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-11-29
 */
public interface LogisticsLargeService extends SuperService<LogisticsLargeEntity> {

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    PagingVO<LogisticsLargeDTO.PagingViewDTO> paging(PagingDTO<LogisticsLargeDTO.PagingParamDTO> dto);

    /**
     * tab页
     *
     * @param dto
     * @return
     */
    List<LogisticsLargeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author Luo_WG
     * @date: 2024-11-29
     */
    BaseResultDTO.AddDTO add(LogisticsLargeDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author Luo_WG
     * @date: 2024-11-29
     */
    Boolean update(LogisticsLargeDTO.UpdateDTO dto);


    /**
     * 删除
     *
     * @param id
     * @return
     */
    BatchResultDTO delete(String id);

    /**
     * 头程费用分摊下推物流大表
     * @param resultDTOS
     * @param skuCostAllocationDetailEntities
     * @param deliveryEntities
     * @param firstMileDeliveryDetailEntities
     * @param entity
     * @param skuCostAllocationEntityList
     * @return
     */
    BatchResultDTO generateFirstMileLogistics(List<BatchResultDTO> resultDTOS,
                                                    List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntities,
                                                    List<FirstMileDeliveryEntity> deliveryEntities,
                                                    List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities,
                                                    FirstMileCostAllocationEntity entity,
                                                    List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList);

    /**
     * 根据来源id查询物流大表
     * @param ids
     * @return
     */
    List<LogisticsLargeEntity> listByIdSourceId(List<String> ids);

    /**
     * 根据单号查询物流大表
     * @param outstockCode
     * @return
     */
    List<LogisticsLargeEntity> listByIdOutstockCode(List<String> outstockCode);

    /**
     * 根据来源详情id查询物流大表
     * @param ids
     * @return
     */
    List<LogisticsLargeEntity> listByIdSourceDetailIds(List<String> ids);

    /**
     * 小包分摊下推物流大表
     * @Author Luo_WG
     * @Date 2024/12/5 17:16
     * @param smallBagCostAllocationMainEntity
     * @param costAllocationEntities
     * @param costAllocationDetailEntityList
     * @param soOutstockEntity
     * @param soOutstockDetailEntities
     * @return java.util.List<com.common.business.dto.base.BatchResultDTO>
     **/
    BatchResultDTO generateSmallBagCostAllocationTable(SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity, List<SmallBagCostAllocationEntity> costAllocationEntities, List<SmallBagCostAllocationDetailEntity> costAllocationDetailEntityList, SoOutstockEntity soOutstockEntity, List<SoOutstockDetailEntity> soOutstockDetailEntities);

    /**
     * 中转费用分摊生成物流大表
     * @param mainEntity
     * @param costAllocationEntityList
     * @param costAllocationDetailEntityList
     * @param reconciliationEntity
     * @param reconciliationDetailEntity
     * @param soOutstockEntity
     * @return
     */
    BatchResultDTO generateTransferCostAllocationTable(TransferDeclareCostAllocationMainEntity mainEntity, List<TransferDeclareCostAllocationEntity> costAllocationEntityList, List<TransferDeclareCostAllocationDetailEntity> costAllocationDetailEntityList, TmsB2cDeclareReconciliationEntity reconciliationEntity, TmsB2cDeclareReconciliationDetailEntity reconciliationDetailEntity, SoOutstockEntity soOutstockEntity);

    /**
     * 导出Excel
     * @param dto
     * @return
     */
    Boolean exportLogisticsLarge(LogisticsLargeDTO.ExportDTO dto);


}
