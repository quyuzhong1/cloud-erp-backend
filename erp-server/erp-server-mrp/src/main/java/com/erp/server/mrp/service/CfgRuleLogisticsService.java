package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;

import java.util.List;

/**
 * <p>
 * 备货物流（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleLogisticsService extends SuperService<CfgRuleLogisticsEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param logisticsList
    * @return
    */
    Boolean update(List<CfgRuleLogisticsDTO.UpdateDTO> logisticsList,String stockUpId);

    /**
     * 根据备货主表id查询
     * @author will
     * @date 2024/8/23 16:36
     * @param stockUpIdList
     * @return List<CfgRuleLogisticsEntity>
     */
    List<CfgRuleLogisticsEntity> listByStockUpIdList (List<String> stockUpIdList);

    /**
     * 根据备货主表id查询
     * @author will
     * @date 2024/8/23 16:36
     * @param stockUpIdList
     * @return List<CfgRuleLogisticsDTO.ViewDTO>
     */
    List<CfgRuleLogisticsDTO.ViewDTO> listViewByStockUpIdList (List<String> stockUpIdList);
    /**
     * 根据备货主表id删除
     * @author will
     * @date 2024/8/29 16:32
     * @param stockUpId
     */
    void deleteByStockUpId(String stockUpId);
}
