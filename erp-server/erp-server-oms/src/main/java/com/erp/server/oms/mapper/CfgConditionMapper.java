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
     * 获取所有的条件
     * @author yl
     * @date 2023-10-08 14:46
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CfConditionDTO.ListDTO>
     */
    List<CfgConditionDTO.ListDTO> listAllCondition();

    /**
     * 申报规则列表查询
     * @return
     */
    List<CfgConditionDTO.ListDTO> listDeclareCondition();
    /**
     * @description: 订单处理条件下拉
     * @author Will
     * @date: 2024/5/9 14:35
     * @return List<ListDTO>
     */
    List<CfgConditionDTO.ListDTO> listOrderHandleCondition();
}
