package com.erp.server.tms.service;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileSkuCostAllocationDetailDTO;

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
}
