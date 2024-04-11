package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.SettingForecastChannelDTO;
import com.erp.model.tms.entity.SettingForecastChannelEntity;

import java.util.List;

/**
 * <p>
 * 预报设置渠道表 服务类
 * </p>
 *
 * @author will
 * @since 2024-04-02
 */
public interface SettingForecastChannelService extends SuperService<SettingForecastChannelEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-04-02
    * @param addDTOList
    * @return
    */
    BaseResultDTO.AddDTO add(List<SettingForecastChannelDTO.AddDTO> addDTOList);

    /**
     * @description: 根据渠道id集合查询
     * @author Will
     * @date: 2024/4/2 14:55
     * @param logisticsChannelIdList
     * @return List<SettingForecastChannelEntity>
     */
    List<SettingForecastChannelEntity> listByLogisticsChannelIdList (List<String> logisticsChannelIdList);
    /**
     * @description: 根据主表id集合查询
     * @author Will
     * @date: 2024/4/2 15:27
     * @param mainIdList
     * @return List<SettingForecastChannelEntity>
     */
    List<SettingForecastChannelEntity> listByMainIdList(List<String> mainIdList);
    /**
     * @description: 删除渠道
     * @author Will
     * @date: 2024/4/2 15:51
     * @param deleteIdList
     * @return Boolean
     */
    Boolean deleteByMainIdList(List<String> deleteIdList);
}
