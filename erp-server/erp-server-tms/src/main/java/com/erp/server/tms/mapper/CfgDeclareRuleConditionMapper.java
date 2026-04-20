package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.CfgDeclareRuleConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.CfgDeclareRuleConditionDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 报关规则条件表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-04-20
 */
@Mapper
public interface CfgDeclareRuleConditionMapper extends BaseMapper<CfgDeclareRuleConditionEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgDeclareRuleConditionDTO.ListDTO> paging(Page query, @Param("params") CfgDeclareRuleConditionDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgDeclareRuleConditionDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgDeclareRuleConditionDTO.ListDTO> listExport(@Param("params") CfgDeclareRuleConditionDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgDeclareRuleConditionDTO.TabListDTO> tabList(@Param("params") CfgDeclareRuleConditionDTO.PagingParamDTO searchParam);
}
