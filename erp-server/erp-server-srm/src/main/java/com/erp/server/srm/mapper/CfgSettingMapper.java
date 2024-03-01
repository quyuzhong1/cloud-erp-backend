package com.erp.server.srm.mapper;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 系统配置管理 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-01-10
 */
@Mapper
public interface CfgSettingMapper extends BaseMapper<CfgSettingEntity> {

    List<CfgSettingEntity> getListBySupplierId(@Param("supplierId") String supplierId);
}
