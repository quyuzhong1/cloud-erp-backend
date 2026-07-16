package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报关规则主表 mapper
 */
@Mapper
public interface CfgDeclareRuleMapper extends BaseMapper<CfgDeclareRuleEntity> {

    List<CfgDeclareRuleDTO.ListDTO> paging(@Param("params") CfgDeclareRuleDTO.ListParamDTO params);

    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgDeclareRuleDTO.PagingParamDTO params);

    List<CfgDeclareRuleDTO.ListDTO> listExport(@Param("params") CfgDeclareRuleDTO.ExportDTO params);

    List<CfgDeclareRuleDTO.TabListDTO> tabList(@Param("params") CfgDeclareRuleDTO.PagingParamDTO searchParam);
}
