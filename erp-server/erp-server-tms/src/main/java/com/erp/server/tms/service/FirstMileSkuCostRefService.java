package com.erp.server.tms.service;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationEntity;
import com.erp.model.tms.entity.FirstMileSkuCostRefEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileSkuCostRefDTO;

import java.util.List;

/**
 * <p>
 * sku成本关系记录 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-24
 */
public interface FirstMileSkuCostRefService extends SuperService<FirstMileSkuCostRefEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileSkuCostRefDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-24
    * @param dto
    * @return
    */
    Boolean update(FirstMileSkuCostRefDTO.UpdateDTO dto);

    /**
     * 保存sku成本使用记录
     * @param skuCostDetailIds 成本id
     * @param id 引用id
     */
    void saveOrUpdateRef(List<String> skuCostDetailIds, String id);

    /**
     * 删除sku成本关联关系-sku费用分摊记录
     * @param skuCostAllocationEntityList
     */
    void removeBySkuCostAllocation(List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList);

    /**
     * 根据成本明细ids查询sku成本明细记录
     * @param detailIds
     * @return
     */
    List<FirstMileSkuCostRefEntity> listBySkuCostDetailIds(List<String> detailIds);
}
