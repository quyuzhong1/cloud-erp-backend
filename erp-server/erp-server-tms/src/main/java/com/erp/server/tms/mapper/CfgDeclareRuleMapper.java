package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 报关规则主表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-04-20
 */
@Mapper
public interface CfgDeclareRuleMapper extends BaseMapper<CfgDeclareRuleEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgDeclareRuleDTO.ListDTO> paging(Page query, @Param("params") CfgDeclareRuleDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgDeclareRuleDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgDeclareRuleDTO.ListDTO> listExport(@Param("params") CfgDeclareRuleDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgDeclareRuleDTO.TabListDTO> tabList(@Param("params") CfgDeclareRuleDTO.PagingParamDTO searchParam);
}
