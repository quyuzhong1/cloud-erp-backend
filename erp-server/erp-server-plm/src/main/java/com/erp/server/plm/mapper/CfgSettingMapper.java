package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.PlmCfgSettingEntity;
import com.erp.model.wms.entity.CfgRuleOutEntity;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface CfgSettingMapper extends BaseMapper<PlmCfgSettingEntity> {

}
