package com.erp.server.dmp.mapper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.dmp.entity.DictRuleConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 条件字典表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2025-01-20
 */
@Mapper
public interface DictRuleConditionMapper extends BaseMapper<DictRuleConditionEntity> {

    List<BaseDropDownDTO.CommonDTO> listRuleField();
}
