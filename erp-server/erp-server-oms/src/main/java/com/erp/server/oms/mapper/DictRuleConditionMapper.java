package com.erp.server.oms.mapper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.entity.DictRuleConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 条件字典表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Mapper
public interface DictRuleConditionMapper extends BaseMapper<DictRuleConditionEntity> {

    /**
     * 根据key 获取到下拉选项
     * @author yl
     * @date 2023-08-31 11:50
     * @param type
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     */
    List<BaseDropDownDTO.CommonDTO> listByType(@Param("type") String type);

    List<BaseDropDownDTO.CommonDTO> listRuleField();
}
