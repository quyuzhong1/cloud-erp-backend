package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 18:14
 */
public interface BiDataSourceCostDetailService  extends IService<BiDataSourceCostDetailEntity> {
    /**
     * 根据costId 查询记录，并进行行列转换
     *
     * @param costIds
     * @param dictValues
     * @return
     */
    HashMap<String, Map<String, BigDecimal>> convertListByCostIds(List<String> costIds, List<String> dictValues);
    /**
     * @description:
     * @author Will
     * @date: 2022/12/16 15:14
     * @param costIds
     * @return List<BiDataSourceCostDetailEntity>
     */
    List<BiDataSourceCostDetailEntity> listByCostIds(List<String> costIds);
    /**
     * @description: 根据成本id删除明细
     * @author Will
     * @date: 2022/12/27 14:06
     * @param costId
     */
    void removeByCostId(String costId);
}
