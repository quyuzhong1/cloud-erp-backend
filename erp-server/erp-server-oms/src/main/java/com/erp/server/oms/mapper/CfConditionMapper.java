package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.CfConditionDTO;
import com.erp.model.oms.entity.CfConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 条件配置表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Mapper
public interface CfConditionMapper extends BaseMapper<CfConditionEntity> {

    /**
     * 获取到对应逻辑关系
     * @author yl
     * @date 2023-08-31 12:26
     * @param conditionCode
     * @return java.util.List<com.erp.model.oms.dto.CfConditionDTO.CommonDTO>
     */
    List<CfConditionDTO.CommonDTO> listByConditionCode(@Param("conditionCode") String conditionCode);
}
