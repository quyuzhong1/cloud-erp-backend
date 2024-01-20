package com.erp.server.tms.mapper;

import com.erp.model.tms.entity.SettingForecastEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 预报设置 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-01-18
 */
@Mapper
public interface SettingForecastMapper extends BaseMapper<SettingForecastEntity> {

    /**
     * 根据渠道id 查询
     * @param logisticsChannelId
     * @return
     */
    SettingForecastEntity getByLogisticsChannelId(@Param("logisticsChannelId") String logisticsChannelId);
}
