package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.CfgRulePickingStagingDTO;
import com.erp.model.wms.entity.CfgRulePickingStagingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 拣货暂存规则 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@Mapper
public interface CfgRulePickingStagingMapper extends BaseMapper<CfgRulePickingStagingEntity> {

    List<CfgRulePickingStagingDTO.StagingDTO> viewStaging();
}
