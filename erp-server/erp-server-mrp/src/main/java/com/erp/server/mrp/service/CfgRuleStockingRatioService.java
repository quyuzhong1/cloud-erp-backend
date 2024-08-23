package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
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
    * 新增
    * @author will
    * @date: 2024-08-23
    * @param stockingRatioList
    * @return
    */
    Boolean add(List<CfgRuleStockingRatioDTO.AddDTO> stockingRatioList,String stockUpId,String type);

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param stockingRatioList
    * @return
    */
    Boolean update(List<CfgRuleStockingRatioDTO.UpdateDTO> stockingRatioList,String stockUpId,String type);

    /**
     * 根据备货主表id查询
     * @author will
     * @date 2024/8/23 16:56
     * @param stockUpIdList
     * @return List<CfgRuleStockingRatioEntity>
     */
    List<CfgRuleStockingRatioEntity> listByStockUpIdList (List<String> stockUpIdList);


}
