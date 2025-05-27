package com.erp.server.sys.mapper;
import com.erp.model.sys.dto.CfgConditionDTO;
import com.erp.model.sys.entity.CfgConditionEntity;
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
 * @since 2025-05-23
 */
@Mapper
public interface CfgConditionMapper extends BaseMapper<CfgConditionEntity> {

    List<CfgConditionDTO.CommonDTO> listByType(@Param("type")String type);
}
