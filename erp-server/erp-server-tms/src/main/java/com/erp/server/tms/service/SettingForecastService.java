package com.erp.server.tms.service;

import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.SettingForecastEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 预报设置 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-18
 */
public interface SettingForecastService extends SuperService<SettingForecastEntity> {


    /**
     * 获取所有的预报设置
     * @description
     * @author Lambda
     * @return
     * @create 2024-01-18 16:08
     */
    List<SettingForecastDTO.ListDTO> listAll();

    /**
     * 添加修该预报设置
     * @description
     * @param list
     * @author Lambda
     * @return 
     * @create 2024-01-18 16:32
     */
    Boolean addOrUpdate(List<SettingForecastDTO.SaveOrUpdateDTO> list);

    
    /**
     * 删除
     * @description
     * @param idList
     * @author Lambda
     * @return 
     * @create 2024-01-18 18:59
     */
    Boolean delete(List<String> idList);

    
    /**
     * 物流商列表
     * @description
     * @author Lambda
     * @return 
     * @create 2024-01-19 9:39
     */
    List<BaseDropDownDTO.DisabledDTO> listLogisticsSupplier();

    /**
     * 根据渠道id 获取到 有效的预报设置
     * @description
     * @param logisticsChannelId
     * @author Lambda
     * @return 
     * @create 2024-01-19 17:51
     */
    SettingForecastDTO.ForecastStatusDTO getByLogisticsChannelId(String logisticsChannelId);
}
