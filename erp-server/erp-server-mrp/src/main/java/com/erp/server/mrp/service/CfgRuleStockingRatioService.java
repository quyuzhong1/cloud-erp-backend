package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.entity.CfgRuleStockingRatioEntity;

import java.util.List;

/**
 * <p>
 * 备货系数（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleStockingRatioService extends SuperService<CfgRuleStockingRatioEntity> {

    /**
     * 修改
     *
     * @param stockingRatioList
     * @param cfgRuleStockUpEntity
     * @return
     * @author will
     * @date: 2024-08-23
     */
    Boolean update(List<CfgRuleStockingRatioDTO.UpdateDTO> stockingRatioList, CfgRuleStockUpEntity cfgRuleStockUpEntity, String type, Boolean isBatch);

    /**
     * 根据备货主表id查询
     * @author will
     * @date 2024/8/23 16:56
     * @param stockUpIdList
     * @return List<CfgRuleStockingRatioEntity>
     */
    List<CfgRuleStockingRatioEntity> listByStockUpIdList (List<String> stockUpIdList);

    /**
     * 根据备货主表id和类型查询
     * @author will
     * @date 2024/8/29 11:48
     * @param stockUpIdList
     * @param type
     * @return List<CfgRuleStockingRatioEntity>
     */
    List<CfgRuleStockingRatioEntity> listByStockUpIdListAndType (List<String> stockUpIdList,String type);
    /**
     * 根据备货主表id删除
     * @author will
     * @date 2024/8/29 16:49
     * @param stockUpId
     */
    void deleteByStockUpId(String stockUpId);

    /**
     * 根据备货主表id和sku类型查询
     *
     * @param id      备货主表id
     * @param skuType sku类型
     */
    List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> listByStockUpIdAndType(String id, String skuType);
}
