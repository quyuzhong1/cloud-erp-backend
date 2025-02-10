package com.erp.server.dmp.mapper;
import com.erp.model.dmp.dto.CfgConditionDTO;
import com.erp.model.dmp.entity.CfgConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 条件配置表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2025-01-20
 */
@Mapper
public interface CfgConditionMapper extends BaseMapper<CfgConditionEntity> {

    List<CfgConditionDTO.ListDTO> listDeclareCondition();
}
