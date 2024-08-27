package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.entity.CfgRuleCommonEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 公共配置（规则设置） Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Mapper
public interface CfgRuleCommonMapper extends BaseMapper<CfgRuleCommonEntity> {
    /**
     * 查询树结构数据
     * @author will
     * @date 2024/8/26 9:46
     * @param platformType
     * @param isEnableOverseas
     * @return List<ViewDTO>
     */
    List<CfgRuleCommonDTO.ViewDTO> listRuleCommon(@Param("platformType") String platformType,@Param("type") String type,@Param("isEnableOverseas") Boolean isEnableOverseas);
    /**
     * 
     * @author will
     * @date 2024/8/26 14:38
     * @param platformType
     * @param isEnableOverseas
     * @return List<ViewDTO>
     */
    List<CfgRuleCommonDTO.ViewDTO> listDefaultRuleCommon(@Param("platformType")String platformType,@Param("type") String type,@Param("isEnableOverseas") Boolean isEnableOverseas);
}
