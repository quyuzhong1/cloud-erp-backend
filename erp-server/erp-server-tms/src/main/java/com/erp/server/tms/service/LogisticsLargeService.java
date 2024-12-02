package com.erp.server.tms.service;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationEntity;
import com.erp.model.tms.entity.LogisticsLargeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsLargeDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;

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
    * 新增
    * @author Luo_WG
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsLargeDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    Boolean update(LogisticsLargeDTO.UpdateDTO dto);

    /**
     *
     * @Author Luo_WG
     * @Date 2024/12/2 9:45
     * @param entity
     * @param firstMileSkuCostAllocationEntity
     * @param skuCostDetailEntityList
     * @param deliveryEntity
     * @param deliveryDetailEntities
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO generateFirstMileLogisticsTable(FirstMileCostAllocationEntity entity, FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity, List<FirstMileSkuCostAllocationDetailEntity> skuCostDetailEntityList, FirstMileDeliveryEntity deliveryEntity, List<FirstMileDeliveryDetailEntity> deliveryDetailEntities);

    /**
     * 查询需要添加物流大表的数据
     * @param ids
     */
    void listLargeDataById(List<String> ids);
}
