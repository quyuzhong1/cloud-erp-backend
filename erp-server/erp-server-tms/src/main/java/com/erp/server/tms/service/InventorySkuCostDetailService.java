package com.erp.server.tms.service;
import com.erp.model.tms.entity.InventorySkuCostDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;
import com.erp.model.tms.entity.InventorySkuCostEntity;

import java.util.List;

/**
 * <p>
 * SKU成本明细 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
 */
public interface InventorySkuCostDetailService extends SuperService<InventorySkuCostDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InventorySkuCostDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-16
    * @param dto
    * @return
    */
    Boolean update(InventorySkuCostDetailDTO.UpdateDTO dto);

    /**
     * 根据主表id删除明细
     * @param id
     */
    void removeByMainId(String id);

    /**
     * 根据主表查询明细列表
     * @param strings
     * @return
     */
    List<InventorySkuCostDetailEntity> listByMainIds(List<String> strings);

    /**
     * 处理明细数据
     * @param detailEntityList
     * @param entity
     */
    void buildDetail(List<InventorySkuCostDetailEntity> detailEntityList, InventorySkuCostEntity entity);
}
