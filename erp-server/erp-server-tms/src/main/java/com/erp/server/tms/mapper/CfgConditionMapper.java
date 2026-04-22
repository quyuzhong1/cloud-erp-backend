package com.erp.server.tms.mapper;

import com.erp.model.tms.dto.CfgConditionDTO;
import com.erp.model.tms.entity.CfgConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 条件配置表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-04-22
 */
@Mapper
public interface CfgConditionMapper extends BaseMapper<CfgConditionEntity> {

    List<CfgConditionDTO.CommonDTO> listByType(@Param("type") String type);
}
