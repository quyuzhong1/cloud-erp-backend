package com.erp.server.dmp.service;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.common.business.service.SuperService;
import com.erp.model.dmp.enums.SettingEnum;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 服务配置表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
public interface CfgSettingService extends SuperService<CfgSettingEntity> {

    /**
     * 获取配置通過key批量获取
     * @param keys
     * @return
     */
    Map<SettingEnum, String> getMap(List<SettingEnum> keys);

    /**
     * 通过类型获取配置
     * @param type
     * @return
     */
    Map<SettingEnum, String> getMap(String type);

    /**
     * 通过key获取配置
     * @param key
     * @return
     */

    String getValue(SettingEnum key);

    /**
     * 获取platform_api_task延时配置
     */
    Map<String, Integer> getApiTaskDelaySecond(SettingEnum settingEnum);
    /**
     * 是否支持推送金蝶仓位
     * @author will
     * @date 2024/8/15 18:16
     * @param warehouseIdList
     * @return Boolean
     */
    List<CfgSettingDTO.WarehouseLocationSettingDTO> isPushKingdeeWarehouseLocation(List<String> warehouseIdList);

}
