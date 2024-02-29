package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * <p>
 * 系统配置管理 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-02-29
 */
@Mapper
public interface CfgSettingMapper extends BaseMapper<CfgSettingEntity> {
    /**
     * @description: 查询所有配置
     * @author zdy
     * @date: 2024/1/11 16:58
     * @return List<CfgSettingEntity>
     */
    List<CfgSettingEntity> listCfgSetting();

    /**
     * 根据key查询配置
     * @Author zdy
     * @Date 2024/1/15 14:47
     * @param key
     * @return com.erp.model.wms.entity.CfgSettingEntity
     **/
    CfgSettingEntity getByKey(@RequestParam("key") String key);
}
