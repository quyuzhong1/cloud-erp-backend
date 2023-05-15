package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import com.erp.model.wms.entity.InitStockDetailEntity;

import java.util.List;

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

}
