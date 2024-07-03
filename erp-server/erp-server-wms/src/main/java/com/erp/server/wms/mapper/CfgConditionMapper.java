package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.pickingstrategy.CfgConditionDTO;
import com.erp.model.wms.entity.CfgConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 条件配置表 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-06-03
 */
@Mapper
public interface CfgConditionMapper extends BaseMapper<CfgConditionEntity> {

    List<CfgConditionDTO.CommonDTO> listByType(@Param("type") String type);
}
