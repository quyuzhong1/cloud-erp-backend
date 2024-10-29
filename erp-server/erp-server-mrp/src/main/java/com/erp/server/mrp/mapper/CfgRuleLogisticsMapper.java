package com.erp.server.mrp.mapper;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 备货物流（规则设置） Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Mapper
public interface CfgRuleLogisticsMapper extends BaseMapper<CfgRuleLogisticsEntity> {
    /**
     * 物流下拉
     * @author will
     * @date 2024/10/29 10:29
     * @param paramDTO
     * @return List<SelectLogisticsDTO>
     */
    List<CfgRuleLogisticsDTO.SelectLogisticsDTO> selectLogistics(@Param("params") CfgRuleLogisticsDTO.SelectLogisticsParamDTO paramDTO);
}
