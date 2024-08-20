package com.erp.server.tms.service;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileSkuCostAllocationDTO;

/**
 * <p>
 * 头程费用SKU分摊 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
public interface FirstMileSkuCostAllocationService extends SuperService<FirstMileSkuCostAllocationEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileSkuCostAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    Boolean update(FirstMileSkuCostAllocationDTO.UpdateDTO dto);

    /**
     * 根据主表id删除明细
     * @param id
     */
    void removeByMainId(String id);
}
