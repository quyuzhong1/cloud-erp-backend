package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import com.erp.model.wms.entity.InitStockDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * @Classname: InitStockDetailService
 * @Description: TODO
 * @CreateTime: 2023-05-11  10:31
 * @Author: zhangchunlin
 */
public interface InitStockDetailService  extends SuperService<InitStockDetailEntity> {

    /**
     * 根据主单id获取明细数据
     * @param mainId
     * @return
     */
    List<InitStockDetailEntity> findList(String mainId);

    /**
     * 新增明细
     * @param details
     * @param mainId
     */
    void add(List<InitStockDetailDTO.AddDTO> details, String mainId);

    /**
     * 根据主单id和sku id查询明细
     * @param mainId
     * @param skuId
     * @return
     */
    InitStockDetailEntity findDetail(String mainId, String skuId);


    /**
     * 更新明细
     * @param details
     * @param mainId
     */
    void update(List<InitStockDetailDTO.UpdateDTO> details, String mainId);

    /**
     * 根据仓库、sku id、状态获取已经存在的数据
     * @param warehouseId
     * @param warehouseLocation
     * @param skuId
     * @param mainId
     * @return
     */
    Integer countCondition(String warehouseId, String warehouseLocation, String skuId, String mainId);

    /**
     * 根据主单id集合获取明细数据
     * @param mainIds
     * @return
     */
    Map<String, List<InitStockDetailEntity>> findListByIds(List<String> mainIds);

    /**
     * 根据主单id集合删除期初库存明细数据
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);

}
