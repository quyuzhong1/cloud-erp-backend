package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
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
     *
     * @param logisticsList
     * @param cfgRuleExpireTime
     * @return
     * @author will
     * @date: 2024-08-23
     */
    Boolean update(List<CfgRuleLogisticsDTO.UpdateDTO> logisticsList, CfgRuleExpireTimeEntity cfgRuleExpireTime, Boolean isBatch, boolean isOverseas);

    /**
     * 根据备货主表id查询
     *
     * @param expireTimeIdList
     * @return List<CfgRuleLogisticsEntity>
     * @author will
     * @date 2024/8/23 16:36
     */
    List<CfgRuleLogisticsEntity> listByExpireTimeIdList(List<String> expireTimeIdList);

    /**
     * 根据备货主表id查询
     *
     * @param expireTimeIdList
     * @return List<CfgRuleLogisticsDTO.ViewDTO>
     * @author will
     * @date 2024/8/23 16:36
     */
    List<CfgRuleLogisticsDTO.ViewDTO> listViewByExpireTimeIdList(List<String> expireTimeIdList);

    /**
     * 根据备货主表id删除
     *
     * @param expireTimeId
     * @author will
     * @date 2024/8/29 16:32
     */
    void deleteByExpireTimeId(String expireTimeId);

    /**
     * 下拉物流信息
     *
     * @return List<SelectLogisticsDTO>
     * @author will
     * @date 2024/10/29 10:19
     */
    List<CfgRuleLogisticsDTO.SelectLogisticsDTO> selectLogistics(CfgRuleLogisticsDTO.SelectLogisticsParamDTO paramDTO);
}
