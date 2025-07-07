package com.erp.server.scm.mapper;
import com.erp.model.scm.dto.CfgConditionDTO;
import com.erp.model.scm.entity.CfgConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-06-16
 */
@Mapper
public interface CfgConditionMapper extends BaseMapper<CfgConditionEntity> {

    List<CfgConditionDTO.CommonDTO> listByType(@Param("type")String type);
}
