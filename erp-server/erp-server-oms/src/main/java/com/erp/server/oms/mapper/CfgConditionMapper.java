package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.dto.CfgConditionDTO;
import com.erp.model.oms.entity.CfgConditionEntity;
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
public interface CfgConditionMapper extends BaseMapper<CfgConditionEntity> {

    /**
     * 获取到对应逻辑关系
     * @author yl
     * @date 2023-08-31 12:26
     * @param conditionCode
     * @return java.util.List<com.erp.model.oms.dto.CfConditionDTO.CommonDTO>
     */
    List<CfgConditionDTO.CommonDTO> listByConditionCode(@Param("conditionCode") String conditionCode);
    
    /**
     * 根据条件获取所有的下拉
     * @author ZDY
     * @date 2025-05-23 14:46
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CfConditionDTO.ListDTO>
     */
    List<CfgConditionDTO.ListDTO> listConditionByType(@Param("codeList") List<String> codeList, @Param("sourceType") String sourceType);
}
