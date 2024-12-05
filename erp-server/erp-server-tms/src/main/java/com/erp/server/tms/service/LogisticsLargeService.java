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
     * @param entity
     * @param firstMileSkuCostAllocationEntity
     * @param skuCostDetailEntityList
     * @param deliveryEntity
     * @param deliveryDetailEntities
     * @return com.common.business.dto.base.BatchResultDTO
     * @Author Luo_WG
     * @Date 2024/12/2 9:45
     **/
    BatchResultDTO generateFirstMileLogisticsTable(FirstMileCostAllocationEntity entity, FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity, List<FirstMileSkuCostAllocationDetailEntity> skuCostDetailEntityList, FirstMileDeliveryEntity deliveryEntity, List<FirstMileDeliveryDetailEntity> deliveryDetailEntities);

    /**
     * 根据来源id查询物流大表
     *
     * @param ids
     */
    List<LogisticsLargeEntity> listByIdSourceId(List<String> ids);


    /**
     * 小包分摊下推物流大表
     *
     * @param costAllocationEntity         小包分摊主表
     * @param costAllocationDetailEntities 小包分摊明细表
     * @param soOutstockEntity             销售出库主表
     * @param soOutstockDetailEntity       销售出库明细信息
     * @return com.common.business.dto.base.BatchResultDTO
     * @Author Luo_WG
     * @Date 2024/12/3 15:37
     **/
    /**
     * 小包分摊下推物流大表
     * @Author Luo_WG
     * @Date 2024/12/5 17:16
     * @param [smallBagCostAllocationMainEntity]
     * @return java.util.List<com.common.business.dto.base.BatchResultDTO>
     **/
    List<BatchResultDTO> generateSmallBagCostAllocationTable(SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity);

    /**
     * 中转费用分摊生成物流大表
     * @Author Luo_WG
     * @Date 2024/12/4 16:33
     * @param entity
     * @param detailEntityList
     * @param declareReconciliationEntity
     * @param declareReconciliationDetailEntity
     * @param soOutstockEntity
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO generateTransferCostAllocationTable(TransferDeclareCostAllocationEntity entity, List<TransferDeclareCostAllocationDetailEntity> detailEntityList, TmsB2cDeclareReconciliationEntity declareReconciliationEntity, TmsB2cDeclareReconciliationDetailEntity declareReconciliationDetailEntity, SoOutstockEntity soOutstockEntity);

    /**
     * 导出Excel
     * @param dto
     * @return
     */
    Boolean exportLogisticsLarge(LogisticsLargeDTO.ExportDTO dto);
}
