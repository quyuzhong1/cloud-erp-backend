package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 系统配置管理 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
@Mapper
public interface CfgSettingMapper extends BaseMapper<CfgSettingEntity> {
    /**
     * @description: 查询所有配置
     * @author Will
     * @date: 2024/1/11 16:58
     * @return List<CfgSettingEntity>
     */
    List<CfgSettingEntity> listCfgSetting();
}
